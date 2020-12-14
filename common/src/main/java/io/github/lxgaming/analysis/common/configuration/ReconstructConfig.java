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

package io.github.lxgaming.analysis.common.configuration;

import io.github.lxgaming.analysis.common.Analysis;
import io.github.lxgaming.reconstruct.common.configuration.Config;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReconstructConfig implements Config {
    
    private final Set<String> transformers = new HashSet<>();
    private final Set<String> excludedPackages = new HashSet<>();
    private Path jarPath;
    private Path mappingPath;
    private Path outputPath;
    
    @Override
    public boolean isDebug() {
        return Analysis.getInstance().getConfig().isDebug();
    }
    
    @Override
    public int getThreads() {
        return Analysis.getInstance().getConfig().getThreads();
    }
    
    @Override
    public void setThreads(int threads) {
        Analysis.getInstance().getConfig().setThreads(threads);
    }
    
    @Override
    public Collection<String> getTransformers() {
        return transformers;
    }
    
    @Override
    public Path getJarPath() {
        return jarPath;
    }
    
    public void setJarPath(Path jarPath) {
        this.jarPath = jarPath;
    }
    
    @Override
    public Path getMappingPath() {
        return mappingPath;
    }
    
    public void setMappingPath(Path mappingPath) {
        this.mappingPath = mappingPath;
    }
    
    @Override
    public Path getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }
    
    @Override
    public Collection<String> getExcludedPackages() {
        return excludedPackages;
    }
}