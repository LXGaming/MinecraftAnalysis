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

package io.github.lxgaming.analysis.common;

import io.github.lxgaming.analysis.common.configuration.Config;
import io.github.lxgaming.analysis.common.configuration.ReconstructConfig;
import io.github.lxgaming.analysis.common.entity.BuildManifest;
import io.github.lxgaming.analysis.common.entity.minecraft.Artifact;
import io.github.lxgaming.analysis.common.entity.minecraft.Version;
import io.github.lxgaming.analysis.common.entity.minecraft.VersionList;
import io.github.lxgaming.analysis.common.entity.minecraft.VersionManifest;
import io.github.lxgaming.analysis.common.manager.QueryManager;
import io.github.lxgaming.analysis.common.util.HashUtils;
import io.github.lxgaming.analysis.common.util.StringUtils;
import io.github.lxgaming.analysis.common.util.Toolbox;
import io.github.lxgaming.analysis.common.util.WebUtils;
import io.github.lxgaming.classloader.ClassLoaderUtils;
import io.github.lxgaming.reconstruct.common.Reconstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Analysis {
    
    public static final String ID = "minecraftanalysis";
    public static final String NAME = "Minecraft Analysis";
    public static final String VERSION = "@version@";
    public static final String AUTHORS = "LX_Gaming";
    public static final String SOURCE = "https://github.com/LXGaming/MinecraftAnalysis";
    public static final String WEBSITE = "https://lxgaming.github.io/";
    public static final String USER_AGENT = Analysis.NAME + "/" + Analysis.VERSION + "; (+" + Analysis.WEBSITE + ")";
    
    private static Analysis instance;
    private final Logger logger;
    private final Config config;
    private final ReconstructConfig reconstructConfig;
    private Path analysisPath;
    private Path versionPath;
    private BuildManifest manifest;
    
    public Analysis(Config config) {
        instance = this;
        this.logger = LoggerFactory.getLogger(Analysis.NAME);
        this.config = config;
        this.reconstructConfig = new ReconstructConfig();
    }
    
    public void load() {
        if (StringUtils.isBlank(getConfig().getVersion())) {
            getLogger().error("Invalid arguments");
            return;
        }
        
        this.analysisPath = Toolbox.getPath()
                .resolve("analysis")
                .resolve(getConfig().getVersion());
        this.versionPath = Toolbox.getPath()
                .resolve("versions")
                .resolve(getConfig().getVersion());
        
        QueryManager.prepare();
        
        if (!downloadMinecraft()) {
            return;
        }
        
        if (getConfig().isReconstruct()) {
            if (!reconstructMinecraft()) {
                return;
            }
        } else {
            getLogger().info("Skipping Reconstruct");
        }
        
        try {
            ClassLoaderUtils.appendToClassPath(getReconstructConfig().getOutputPath().toUri().toURL());
        } catch (Throwable ex) {
            getLogger().error("Encountered an error while attempting to append to the class path", ex);
            return;
        }
        
        manifest = deserializeMinecraftManifest();
        if (manifest == null) {
            return;
        }
        
        getLogger().info("Minecraft v{} ({}):", getManifest().getName(), getManifest().getId());
        getLogger().info("- Build Time: {}", getManifest().getBuildTime());
        getLogger().info("- Stable: {}", getManifest().getStable());
        getLogger().info("- Versions:");
        getLogger().info("-- Pack: {}", getManifest().getPackVersion());
        getLogger().info("-- Protocol: {}", getManifest().getProtocolVersion());
        getLogger().info("-- World: {}", getManifest().getWorldVersion());
        
        getLogger().info("Performing Analysis...");
        
        write("version", manifest);
        QueryManager.execute();
    }
    
    private boolean downloadMinecraft() {
        VersionList versionList;
        try {
            versionList = WebUtils.deserializeJson(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), VersionList.class);
        } catch (Exception ex) {
            getLogger().error("Encountered an error while getting VersionList", ex);
            return false;
        }
        
        Version version = versionList.getVersion(getConfig().getVersion());
        if (version == null) {
            getLogger().error("Cannot find specified version {}", getConfig().getVersion());
            return false;
        }
        
        Analysis.getInstance().getLogger().info("Minecraft v{} ({})", version.getId(), version.getType());
        
        VersionManifest versionManifest;
        try {
            versionManifest = WebUtils.deserializeJson(new URL(version.getUrl()), VersionManifest.class);
        } catch (Exception ex) {
            getLogger().error("Encountered an error while getting VersionManifest", ex);
            return false;
        }
        
        Artifact serverArtifact = versionManifest.getDownloads().get("server");
        if (serverArtifact == null) {
            Analysis.getInstance().getLogger().error("Missing Server Artifact");
            return false;
        }
        
        Artifact serverMappingsArtifact = versionManifest.getDownloads().get("server_mappings");
        if (serverMappingsArtifact == null) {
            Analysis.getInstance().getLogger().error("Missing Server Mappings Artifact");
            return false;
        }
        
        Path jarPath = getVersionPath().resolve("server.jar");
        Path mappingPath = getVersionPath().resolve("server.txt");
        Path outputPath = getVersionPath().resolve("server-deobf.jar");
        
        getReconstructConfig().setJarPath(jarPath);
        getReconstructConfig().setMappingPath(mappingPath);
        getReconstructConfig().setOutputPath(outputPath);
        getReconstructConfig().getExcludedPackages().add("com.google.");
        getReconstructConfig().getExcludedPackages().add("com.mojang.");
        getReconstructConfig().getExcludedPackages().add("io.netty.");
        getReconstructConfig().getExcludedPackages().add("it.unimi.dsi.fastutil.");
        getReconstructConfig().getExcludedPackages().add("javax.");
        getReconstructConfig().getExcludedPackages().add("joptsimple.");
        getReconstructConfig().getExcludedPackages().add("org.apache.");
        
        try {
            if (Files.exists(jarPath) && Files.size(jarPath) == serverArtifact.getSize() && HashUtils.sha1(jarPath, serverArtifact.getHash())) {
                Analysis.getInstance().getLogger().info("Verified Server");
            } else {
                Analysis.getInstance().getLogger().debug("Downloading {}", serverArtifact.getUrl());
                WebUtils.downloadFile(
                        new URL(serverArtifact.getUrl()),
                        jarPath,
                        serverArtifact.getSize(),
                        serverArtifact.getHash());
                
                Analysis.getInstance().getLogger().info("Downloaded Server");
                getConfig().setReconstruct(true);
            }
        } catch (Exception ex) {
            getLogger().error("Encountered an error while downloading {}", serverArtifact.getUrl(), ex);
            return false;
        }
        
        try {
            if (Files.exists(mappingPath) && Files.size(mappingPath) == serverMappingsArtifact.getSize() && HashUtils.sha1(mappingPath, serverMappingsArtifact.getHash())) {
                Analysis.getInstance().getLogger().info("Verified Server Mappings");
            } else {
                Analysis.getInstance().getLogger().debug("Downloading {}", serverMappingsArtifact.getUrl());
                WebUtils.downloadFile(
                        new URL(serverMappingsArtifact.getUrl()),
                        mappingPath,
                        serverMappingsArtifact.getSize(),
                        serverMappingsArtifact.getHash());
                
                Analysis.getInstance().getLogger().info("Downloaded Server Mappings");
                getConfig().setReconstruct(true);
            }
        } catch (Exception ex) {
            getLogger().error("Encountered an error while downloading {}", serverMappingsArtifact.getUrl(), ex);
            return false;
        }
        
        return true;
    }
    
    private BuildManifest deserializeMinecraftManifest() {
        try (InputStream inputStream = getClass().getResourceAsStream("/version.json")) {
            if (inputStream == null) {
                getLogger().error("version.json does not exist");
                return null;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return Toolbox.GSON.fromJson(reader, BuildManifest.class);
            }
        } catch (Exception ex) {
            getLogger().error("Encountered an error while reading version.json", ex);
            return null;
        }
    }
    
    private boolean reconstructMinecraft() {
        try {
            Reconstruct reconstruct = new Reconstruct(getReconstructConfig());
            reconstruct.load();
            return true;
        } catch (Exception ex) {
            getLogger().error("Encountered an error while reconstructing", ex);
            return false;
        } finally {
            Reconstruct.getInstance().shutdown();
        }
    }
    
    public void write(String name, Object object) {
        Path path = getAnalysisPath().resolve(String.format("%s.json", name));
        
        try {
            Files.createDirectories(path.getParent());
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while creating directory {}", path.getParent(), ex);
            return;
        }
        
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            Toolbox.GSON.toJson(object, writer);
            getLogger().info("Wrote {}", path.getFileName());
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while writing to {}", path, ex);
        }
    }
    
    public static Analysis getInstance() {
        return instance;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Config getConfig() {
        return config;
    }
    
    public ReconstructConfig getReconstructConfig() {
        return reconstructConfig;
    }
    
    public Path getAnalysisPath() {
        return analysisPath;
    }
    
    public Path getVersionPath() {
        return versionPath;
    }
    
    public BuildManifest getManifest() {
        return manifest;
    }
}