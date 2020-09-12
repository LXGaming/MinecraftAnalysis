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
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ReconstructService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ReconstructService.class);
    
    public boolean execute(Path path) {
        Path jarPath = getJarPath();
        if (jarPath == null) {
            LOGGER.error("Reconstruct does not exist");
            return false;
        }
        
        Path inputPath = path.resolve("server.jar");
        if (!Files.isRegularFile(inputPath)) {
            LOGGER.error("{} does not exist", inputPath.getFileName().toString());
            return false;
        }
        
        Path mappingPath = path.resolve("server.txt");
        if (!Files.isRegularFile(mappingPath)) {
            LOGGER.error("{} does not exist", mappingPath.getFileName().toString());
            return false;
        }
        
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(path.toFile());
        processBuilder.command(
                "java", "-Xms256M", "-Xmx1G",
                "-jar", jarPath.toString(),
                "--threads", "1",
                "--input", inputPath.toString(),
                "--mapping", mappingPath.toString(),
                "--output", "server-deobf.jar",
                "--exclude", "com.google.,com.mojang.,io.netty.,it.unimi.dsi.fastutil.,javax.,joptsimple.,org.apache.",
                "--agree");
        
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
            return Toolbox.findFile(Paths.get("."), "reconstruct-cli-.*\\.jar");
        } catch (Exception ex) {
            LOGGER.error("Encountered an error while finding file", ex);
            return null;
        }
    }
}