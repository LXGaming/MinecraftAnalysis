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
import io.github.lxgaming.analysis.common.entity.BuildManifest;
import io.github.lxgaming.analysis.common.integration.minecraft.MinecraftIntegration;
import io.github.lxgaming.analysis.common.integration.reconstruct.ReconstructIntegration;
import io.github.lxgaming.analysis.common.manager.IntegrationManager;
import io.github.lxgaming.analysis.common.manager.QueryManager;
import io.github.lxgaming.analysis.common.util.AnalysisClassLoader;
import io.github.lxgaming.analysis.common.util.StringUtils;
import io.github.lxgaming.analysis.common.util.Toolbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.lang.reflect.Method;
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
    public static final String USER_AGENT = Analysis.NAME + "/" + Analysis.VERSION + " (+" + Analysis.WEBSITE + ")";
    
    private static Analysis instance;
    private final Logger logger;
    private final Config config;
    private final AnalysisClassLoader classLoader;
    private Path analysisPath;
    private Path versionPath;
    private BuildManifest manifest;
    
    public Analysis(Config config) {
        instance = this;
        this.logger = LoggerFactory.getLogger(Analysis.NAME);
        this.config = config;
        this.classLoader = new AnalysisClassLoader();
    }
    
    public void load() {
        if (getConfig().getPlatform() == null || StringUtils.isBlank(getConfig().getVersion())) {
            getLogger().error("Invalid arguments");
            return;
        }
        
        this.analysisPath = Toolbox.getPath()
                .resolve("analysis")
                .resolve(getConfig().getVersion());
        this.versionPath = Toolbox.getPath()
                .resolve("versions")
                .resolve(getConfig().getVersion());
        
        IntegrationManager.prepare();
        QueryManager.prepare();
        
        IntegrationManager.execute();
        
        MinecraftIntegration minecraftIntegration = IntegrationManager.getIntegration(MinecraftIntegration.class);
        ReconstructIntegration reconstructIntegration = IntegrationManager.getIntegration(ReconstructIntegration.class);
        if (minecraftIntegration == null || reconstructIntegration == null) {
            return;
        }
        
        if (!minecraftIntegration.downloadMinecraft()) {
            return;
        }
        
        if (getConfig().isReconstruct()) {
            if (!reconstructIntegration.performReconstruction()) {
                return;
            }
        } else {
            getLogger().info("Skipping Reconstruct");
        }
        
        classLoader.addPath(reconstructIntegration.getConfig().getOutputPath());
        
        this.manifest = minecraftIntegration.deserializeBuildManifest();
        if (manifest == null) {
            return;
        }
        
        getLogger().info("Minecraft v{} ({}):", getManifest().getName(), getManifest().getId());
        getLogger().info("- Build Time: {}", getManifest().getBuildTime());
        getLogger().info("- Stable: {}", getManifest().getStable());
        getLogger().info("- Versions:");
        getLogger().info("-- Pack:");
        getLogger().info("--- Data: {}", getManifest().getPackVersion().getData());
        getLogger().info("--- Resource: {}", getManifest().getPackVersion().getResource());
        getLogger().info("-- Protocol: {}", getManifest().getProtocolVersion());
        getLogger().info("-- World: {}", getManifest().getWorldVersion());
        write("version", manifest);
        
        try {
            Class<?> bootstrapClass = classLoader.loadClass("net.minecraft.server.Bootstrap");
            Method method = bootstrapClass.getMethod("bootStrap");
            method.invoke(null);
        } catch (Throwable ex) {
            getLogger().error("Encountered an error while attempting to bootstrap Minecraft", ex);
            return;
        }
        
        getLogger().info("Performing Analysis...");
        QueryManager.execute();
    }
    
    public void write(String name, Object object) {
        Path path = getAnalysisPath().resolve(String.format("%s-%s.json", getConfig().getPlatform(), name));
        
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
    
    public AnalysisClassLoader getClassLoader() {
        return classLoader;
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