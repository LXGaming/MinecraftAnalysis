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

package io.github.lxgaming.analysis.common.integration.reconstruct;

import io.github.lxgaming.analysis.common.Analysis;
import io.github.lxgaming.analysis.common.configuration.ReconstructConfig;
import io.github.lxgaming.analysis.common.integration.Integration;
import io.github.lxgaming.reconstruct.common.Reconstruct;

public class ReconstructIntegration extends Integration {
    
    private final ReconstructConfig config = new ReconstructConfig();
    
    @Override
    public boolean prepare() {
        config.getExcludedPackages().add("com.google.");
        config.getExcludedPackages().add("com.mojang.");
        config.getExcludedPackages().add("io.netty.");
        config.getExcludedPackages().add("it.unimi.dsi.fastutil.");
        config.getExcludedPackages().add("javax.");
        config.getExcludedPackages().add("joptsimple.");
        config.getExcludedPackages().add("org.apache.");
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
    
    public boolean performReconstruction() {
        try {
            Reconstruct reconstruct = new Reconstruct(config);
            reconstruct.load();
            return true;
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error during reconstruction", ex);
            return false;
        } finally {
            Reconstruct.getInstance().shutdown();
        }
    }
    
    public ReconstructConfig getConfig() {
        return config;
    }
}