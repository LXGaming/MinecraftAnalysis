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
import io.github.lxgaming.analysis.common.integration.minecraft.entity.Artifact;
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
        try (InputStream inputStream = getClass().getResourceAsStream("/version.json")) {
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
        
        try {
            if (Files.exists(jarPath) && Files.size(jarPath) == artifact.getSize() && HashUtils.sha1(jarPath, artifact.getHash())) {
                Analysis.getInstance().getLogger().info("Verified {}", platform);
            } else {
                Analysis.getInstance().getLogger().debug("Downloading {}", artifact.getUrl());
                WebUtils.downloadFile(
                        new URL(artifact.getUrl()),
                        jarPath,
                        artifact.getSize(),
                        artifact.getHash());
                
                Analysis.getInstance().getLogger().info("Downloaded {}", platform);
                Analysis.getInstance().getConfig().setReconstruct(true);
            }
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while downloading {}", artifact.getUrl(), ex);
            return false;
        }
        
        try {
            if (Files.exists(mappingPath) && Files.size(mappingPath) == mappingsArtifact.getSize() && HashUtils.sha1(mappingPath, mappingsArtifact.getHash())) {
                Analysis.getInstance().getLogger().info("Verified {} Mappings", platform);
            } else {
                Analysis.getInstance().getLogger().debug("Downloading {}", mappingsArtifact.getUrl());
                WebUtils.downloadFile(
                        new URL(mappingsArtifact.getUrl()),
                        mappingPath,
                        mappingsArtifact.getSize(),
                        mappingsArtifact.getHash());
                
                Analysis.getInstance().getLogger().info("Downloaded {} Mappings", platform);
                Analysis.getInstance().getConfig().setReconstruct(true);
            }
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while downloading {}", mappingsArtifact.getUrl(), ex);
            return false;
        }
        
        return true;
    }
    
    public Version getVersion() {
        return version;
    }
}