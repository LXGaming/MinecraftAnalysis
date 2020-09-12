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

package io.github.lxgaming.minecraftanalysis.client;

import io.github.lxgaming.classloader.ClassLoaderUtils;
import io.github.lxgaming.minecraftanalysis.client.configuration.Config;
import io.github.lxgaming.minecraftanalysis.client.entity.Version;
import io.github.lxgaming.minecraftanalysis.client.manager.QueryManager;
import io.github.lxgaming.minecraftanalysis.client.util.Toolbox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MinecraftAnalysis {
    
    public static final String ID = "minecraftanalysis";
    public static final String NAME = "MinecraftAnalysis";
    public static final String VERSION = "@version@";
    public static final String AUTHORS = "LX_Gaming";
    public static final String SOURCE = "https://github.com/LXGaming/MinecraftAnalysis";
    public static final String WEBSITE = "https://lxgaming.github.io/";
    
    private static MinecraftAnalysis instance;
    private final Logger logger;
    private final Config config;
    private Version version;
    
    public MinecraftAnalysis() {
        instance = this;
        this.logger = LogManager.getLogger(MinecraftAnalysis.NAME);
        this.config = new Config();
    }
    
    public void load() {
        if (getConfig().getInputPath() == null || !Files.isRegularFile(getConfig().getInputPath())) {
            getLogger().error("Input Path is not a file");
            return;
        }
        
        if (getConfig().getAddress() == null && getConfig().getOutputPath() == null) {
            getLogger().error("Address or Output not specified");
            return;
        }
        
        if (getConfig().getAddress() != null && !(getConfig().getAddress().getProtocol().equals("http") || getConfig().getAddress().getProtocol().equals("https"))) {
            getLogger().error("Address protocol is not supported");
        }
        
        if (getConfig().getOutputPath() != null && Files.exists(getConfig().getOutputPath()) && !Files.isDirectory(getConfig().getOutputPath())) {
            getLogger().error("Output Path is not a directory");
        }
        
        QueryManager.prepare();
        
        try {
            ClassLoaderUtils.appendToClassPath(getConfig().getInputPath().toUri().toURL());
        } catch (Throwable ex) {
            getLogger().error("Encountered an error while attempting to append to the class path", ex);
            return;
        }
        
        try (InputStream inputStream = getClass().getResourceAsStream("/version.json")) {
            if (inputStream == null) {
                getLogger().error("version.json does not exist");
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                version = Toolbox.GSON.fromJson(reader, Version.class);
            }
        } catch (Exception ex) {
            getLogger().error("Encountered an error while reading version.json", ex);
            return;
        }
        
        if (version == null) {
            getLogger().error("Missing version information");
            return;
        }
        
        write("version", version);
        
        getLogger().info("Minecraft v{} ({}):", getVersion().getName(), getVersion().getId());
        getLogger().info(" - Build Time: {}", getVersion().getBuildTime());
        getLogger().info(" - Stable: {}", getVersion().getStable());
        getLogger().info(" - Versions:");
        getLogger().info(" -- Pack: {}", getVersion().getPackVersion());
        getLogger().info(" -- Protocol: {}", getVersion().getProtocolVersion());
        getLogger().info(" -- World: {}", getVersion().getWorldVersion());
        
        QueryManager.execute();
    }
    
    public void write(String name, Object object) {
        if (getConfig().getAddress() != null) {
            writeAddress(String.format("%s/%s/%s", getConfig().getAddress(), name, version.getId()), object);
        }
        
        if (getConfig().getOutputPath() != null) {
            writeFile(getConfig().getOutputPath().resolve(String.format("%s.json", name)), object);
        }
    }
    
    private void writeAddress(String url, Object object) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);
            
            try (Writer writer = new OutputStreamWriter(urlConnection.getOutputStream(), StandardCharsets.UTF_8)) {
                Toolbox.GSON.toJson(object, writer);
            }
            
            MinecraftAnalysis.getInstance().getLogger().info("Response Code: {}", urlConnection.getResponseCode());
        } catch (Exception ex) {
            MinecraftAnalysis.getInstance().getLogger().error("Encountered an error while writing to {}", url, ex);
        }
    }
    
    private void writeFile(Path path, Object object) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            Toolbox.GSON.toJson(object, writer);
        } catch (Exception ex) {
            MinecraftAnalysis.getInstance().getLogger().error("Encountered an error while writing to {}", path, ex);
        }
    }
    
    public static MinecraftAnalysis getInstance() {
        return instance;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Config getConfig() {
        return config;
    }
    
    public Version getVersion() {
        return version;
    }
}