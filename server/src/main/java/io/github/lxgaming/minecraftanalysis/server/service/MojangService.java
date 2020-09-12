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

import io.github.lxgaming.minecraftanalysis.server.model.minecraft.Artifact;
import io.github.lxgaming.minecraftanalysis.server.model.minecraft.Version;
import io.github.lxgaming.minecraftanalysis.server.model.minecraft.VersionList;
import io.github.lxgaming.minecraftanalysis.server.model.minecraft.VersionManifest;
import io.github.lxgaming.minecraftanalysis.server.repository.VersionRepository;
import io.github.lxgaming.minecraftanalysis.server.util.HashUtils;
import io.github.lxgaming.minecraftanalysis.server.util.Toolbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class MojangService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MojangService.class);
    
    private final ClientService clientService;
    private final ReconstructService reconstructService;
    private final VersionRepository versionRepository;
    private final WebClient webClient;
    
    @Autowired
    public MojangService(ClientService clientService,
                         ReconstructService reconstructService,
                         VersionRepository versionRepository,
                         WebClient webClient) {
        this.clientService = clientService;
        this.reconstructService = reconstructService;
        this.versionRepository = versionRepository;
        this.webClient = webClient;
    }
    
    @Scheduled(cron = "0 0 12 * * *")
    public void execute() {
        LOGGER.info("MojangService running");
        VersionList versionList = getVersionList();
        if (versionList == null) {
            LOGGER.error("Failed to get VersionList");
            return;
        }
        
        List<Version> versions = new ArrayList<>();
        for (Version version : versionList.getVersions()) {
            if (!version.getType().equals("release")) {
                continue;
            }
            
            // Versions after 1.14.4 which don't have mappings
            if (version.getId().equals("19w34a") || version.getId().equals("19w35a")) {
                continue;
            }
            
            if (!versionRepository.existsById(version.getId())) {
                versions.add(0, version);
            }
            
            if (version.getId().equals("1.14.4")) {
                break;
            }
        }
        
        if (versions.isEmpty()) {
            return;
        }
        
        LOGGER.info("Found {} versions", versions.size());
        for (Version version : versions) {
            LOGGER.info("Processing {}", version.getId());
            VersionManifest versionManifest = getVersionManifest(version.getUrl());
            if (versionManifest == null) {
                LOGGER.error("Failed to get VersionManifest for {}", version.getId());
                continue;
            }
            
            Artifact server = versionManifest.getDownloads().get("server");
            Artifact mappings = versionManifest.getDownloads().get("server_mappings");
            if (server == null || mappings == null) {
                LOGGER.error("Missing Server or Server Mappings for {}", version.getId());
                continue;
            }
            
            Path versionPath = getVersionPath(version.getId());
            Toolbox.createDirectories(versionPath);
            
            Path serverPath = versionPath.resolve("server.jar");
            Path mappingsPath = versionPath.resolve("server.txt");
            
            if (Files.exists(serverPath) && verifyFile(serverPath, server.getSha1())) {
                LOGGER.info("Successfully verified server ({})", server.getSha1());
            } else {
                LOGGER.info("Downloading server...");
                if (!downloadFile(server.getUrl(), serverPath) || !verifyFile(serverPath, server.getSha1())) {
                    LOGGER.error("Failed to download or verify Server for {}", version.getId());
                    continue;
                }
                
                LOGGER.info("Successfully downloaded server ({})", server.getSha1());
            }
            
            if (Files.exists(mappingsPath) && verifyFile(mappingsPath, mappings.getSha1())) {
                LOGGER.info("Successfully verified mappings ({})", mappings.getSha1());
            } else {
                LOGGER.info("Downloading mappings...");
                if (!downloadFile(mappings.getUrl(), mappingsPath) || !verifyFile(mappingsPath, mappings.getSha1())) {
                    LOGGER.error("Failed to download or verify Mappings for {}", version.getId());
                    continue;
                }
                
                LOGGER.info("Successfully downloaded mappings ({})", mappings.getSha1());
            }
        }
        
        for (Iterator<Version> iterator = versions.iterator(); iterator.hasNext(); ) {
            Version version = iterator.next();
            
            LOGGER.info("Reconstructing {}...", version.getId());
            
            Path versionPath = getVersionPath(version.getId());
            if (!reconstructService.execute(versionPath)) {
                iterator.remove();
                LOGGER.info("Failed to reconstruct {}", version.getId());
            }
        }
        
        for (Iterator<Version> iterator = versions.iterator(); iterator.hasNext(); ) {
            Version version = iterator.next();
            
            LOGGER.info("Analyzing {}...", version.getId());
            
            Path versionPath = getVersionPath(version.getId());
            if (!clientService.execute(versionPath)) {
                iterator.remove();
                LOGGER.info("Failed to analyze {}", version.getId());
            }
        }
    }
    
    private VersionList getVersionList() {
        return get("https://launchermeta.mojang.com/mc/game/version_manifest.json", VersionList.class);
    }
    
    private VersionManifest getVersionManifest(String url) {
        return get(url, VersionManifest.class);
    }
    
    private Path getVersionPath(String id) {
        return Paths.get("versions", id).toAbsolutePath().normalize();
    }
    
    private <T> T get(String uri, Class<T> type) {
        return webClient
                .method(HttpMethod.GET)
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(type)
                .onErrorResume(throwable -> {
                    LOGGER.error("Encountered an error while getting {}", uri, throwable);
                    return Mono.empty();
                })
                .block();
    }
    
    private boolean downloadFile(String uri, Path path) {
        try {
            Flux<DataBuffer> flux = webClient
                    .method(HttpMethod.GET)
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class);
            
            DataBufferUtils.write(flux, path, StandardOpenOption.CREATE).block();
            return true;
        } catch (Exception ex) {
            LOGGER.error("Encountered an error while downloading {}", uri, ex);
            return false;
        }
    }
    
    private boolean verifyFile(Path path, String hash) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            String computedHash = HashUtils.sha1(inputStream);
            if (hash.equals(computedHash)) {
                return true;
            }
            
            LOGGER.error("Failed to verify file: {} != {}", hash, computedHash);
            return false;
        } catch (Exception ex) {
            LOGGER.error("Encountered an error while verifying file", ex);
            return false;
        }
    }
}