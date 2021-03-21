/*
 * Copyright 2020 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.analysis.common.integration.minecraft;

import io.github.lxgaming.analysis.common.Analysis;
import io.github.lxgaming.analysis.common.entity.BuildManifest;
import io.github.lxgaming.analysis.common.entity.Platform;
import io.github.lxgaming.analysis.common.integration.Integration;
import io.github.lxgaming.analysis.common.integration.minecraft.entity.Action;
import io.github.lxgaming.analysis.common.integration.minecraft.entity.Artifact;
import io.github.lxgaming.analysis.common.integration.minecraft.entity.Library;
import io.github.lxgaming.analysis.common.integration.minecraft.entity.Rule;
import io.github.lxgaming.analysis.common.integration.minecraft.entity.Version;
import io.github.lxgaming.analysis.common.integration.minecraft.entity.VersionList;
import io.github.lxgaming.analysis.common.integration.minecraft.entity.VersionManifest;
import io.github.lxgaming.analysis.common.integration.reconstruct.ReconstructIntegration;
import io.github.lxgaming.analysis.common.manager.IntegrationManager;
import io.github.lxgaming.analysis.common.util.HashUtils;
import io.github.lxgaming.analysis.common.util.Toolbox;
import io.github.lxgaming.analysis.common.util.WebUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class MinecraftIntegration extends Integration {
    
    private Platform platform;
    private Version version;
    
    @Override
    public boolean prepare() {
        VersionList versionList;
        try {
            versionList = WebUtils.deserializeJson(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), VersionList.class);
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while getting VersionList", ex);
            return false;
        }
        
        this.platform = Analysis.getInstance().getConfig().getPlatform();
        this.version = versionList.getVersion(Analysis.getInstance().getConfig().getVersion());
        if (version == null) {
            Analysis.getInstance().getLogger().error("Cannot find specified version {}", Analysis.getInstance().getConfig().getVersion());
            return false;
        }
        
        Analysis.getInstance().getLogger().info("Minecraft {} ({})", version.getId(), version.getType());
        return true;
    }
    
    @Override
    public void execute() throws Exception {
        // no-op
    }
    
    @Override
    public void shutdown() {
        // no-op
    }
    
    public BuildManifest deserializeBuildManifest() {
        try (InputStream inputStream = Analysis.getInstance().getClassLoader().getResourceAsStream("version.json")) {
            if (inputStream == null) {
                Analysis.getInstance().getLogger().error("version.json does not exist");
                return null;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return Toolbox.GSON.fromJson(reader, BuildManifest.class);
            }
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while reading version.json", ex);
            return null;
        }
    }
    
    public boolean downloadMinecraft() {
        VersionManifest versionManifest;
        try {
            versionManifest = WebUtils.deserializeJson(new URL(version.getUrl()), VersionManifest.class);
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while getting VersionManifest", ex);
            return false;
        }
        
        Artifact artifact = versionManifest.getDownloads().get(platform.toString());
        if (artifact == null) {
            Analysis.getInstance().getLogger().error("Missing {} Artifact", platform);
            return false;
        }
        
        Artifact mappingsArtifact = versionManifest.getDownloads().get(platform + "_mappings");
        if (mappingsArtifact == null) {
            Analysis.getInstance().getLogger().error("Missing {} Mappings Artifact", platform);
            return false;
        }
        
        Path versionPath = Analysis.getInstance().getVersionPath();
        Path jarPath = versionPath.resolve(platform + ".jar");
        Path mappingPath = versionPath.resolve(platform + ".txt");
        Path outputPath = versionPath.resolve(platform + "-deobf.jar");
        
        if (!Analysis.getInstance().getConfig().isReconstruct()) {
            Analysis.getInstance().getConfig().setReconstruct(!Files.exists(outputPath));
        }
        
        ReconstructIntegration integration = IntegrationManager.getIntegration(ReconstructIntegration.class);
        if (integration != null) {
            integration.getConfig().setJarPath(jarPath);
            integration.getConfig().setMappingPath(mappingPath);
            integration.getConfig().setOutputPath(outputPath);
        }
        
        if (platform.isClient()) {
            installLibraries(versionManifest.getLibraries());
        }
        
        return downloadArtifact(artifact, jarPath) && downloadArtifact(mappingsArtifact, mappingPath);
    }
    
    private void installLibraries(Collection<Library> libraries) {
        for (Library library : libraries) {
            if (!checkRules(library.getRules())) {
                continue;
            }
            
            Artifact libraryArtifact = library.getDownloads().getArtifact();
            if (libraryArtifact != null) {
                Path path = Analysis.getInstance().getLibrariesPath().resolve(libraryArtifact.getPath());
                if (downloadArtifact(libraryArtifact, path)) {
                    Analysis.getInstance().getClassLoader().addPath(path);
                }
            }
            
            Artifact nativeArtifact = library.getNative();
            if (nativeArtifact != null) {
                Path path = Analysis.getInstance().getLibrariesPath().resolve(nativeArtifact.getPath());
                if (downloadArtifact(nativeArtifact, path)) {
                    Analysis.getInstance().getClassLoader().addPath(path);
                }
            }
        }
    }
    
    private boolean downloadArtifact(Artifact artifact, Path path) {
        try {
            if (Files.exists(path) && Files.size(path) == artifact.getSize() && HashUtils.sha1(path, artifact.getHash())) {
                Analysis.getInstance().getLogger().info("Verified {}", path.getFileName());
                return true;
            }
            
            Analysis.getInstance().getLogger().info("Downloading {} ({})", path.getFileName(), artifact.getUrl());
            WebUtils.downloadFile(
                    new URL(artifact.getUrl()),
                    path,
                    artifact.getSize(),
                    artifact.getHash());
            
            Analysis.getInstance().getLogger().info("Downloaded {}", path.getFileName());
            return true;
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while downloading {} ({})", path.getFileName(), artifact.getUrl(), ex);
            return false;
        }
    }
    
    private boolean checkRules(Collection<Rule> rules) {
        if (rules == null || rules.isEmpty()) {
            return true;
        }
        
        Action action = Action.DISALLOW;
        for (Rule rule : rules) {
            if (rule.getOperatingSystem() != null && rule.getOperatingSystem().isSupported()) {
                action = rule.getAction();
            }
        }
        
        return action == Action.ALLOW;
    }
    
    public Version getVersion() {
        return version;
    }
}