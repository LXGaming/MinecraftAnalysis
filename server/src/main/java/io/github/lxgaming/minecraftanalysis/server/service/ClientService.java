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

package io.github.lxgaming.minecraftanalysis.server.service;

import io.github.lxgaming.minecraftanalysis.server.util.Toolbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ClientService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientService.class);
    
    private final Integer port;
    
    @Autowired
    private ClientService(Environment environment) {
        this.port = environment.getRequiredProperty("server.port", Integer.class);
    }
    
    public boolean execute(Path path) {
        Path jarPath = getJarPath();
        if (jarPath == null) {
            LOGGER.error("Client does not exist");
            return false;
        }
        
        Path inputPath = path.resolve("server-deobf.jar");
        if (!Files.isRegularFile(inputPath)) {
            LOGGER.error("{} does not exist", inputPath.getFileName().toString());
            return false;
        }
        
        Path outputPath = path.resolve("reports");
        Toolbox.createDirectories(outputPath);
        
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(path.toFile());
        processBuilder.command(
                "java", "-jar", jarPath.toString(),
                "--input", inputPath.toString(),
                "--address", String.format("http://127.0.0.1:%s", port));
        
        Path nullPath = Toolbox.getNullPath();
        if (nullPath != null) {
            processBuilder.redirectError(nullPath.toFile());
            processBuilder.redirectOutput(nullPath.toFile());
        } else {
            processBuilder.inheritIO();
        }
        
        return await(processBuilder);
    }
    
    private boolean await(ProcessBuilder processBuilder) {
        try {
            Process process = processBuilder.start();
            return process.waitFor() == 0;
        } catch (Exception ex) {
            LOGGER.error("Encountered an error while running process", ex);
            return false;
        }
    }
    
    private Path getJarPath() {
        try {
            return Toolbox.findFile(Paths.get("."), "minecraftanalysis-client-.*\\.jar");
        } catch (Exception ex) {
            LOGGER.error("Encountered an error while finding file", ex);
            return null;
        }
    }
}