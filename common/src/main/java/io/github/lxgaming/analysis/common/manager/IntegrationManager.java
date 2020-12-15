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

package io.github.lxgaming.analysis.common.manager;

import io.github.lxgaming.analysis.common.Analysis;
import io.github.lxgaming.analysis.common.integration.Integration;
import io.github.lxgaming.analysis.common.integration.minecraft.MinecraftIntegration;
import io.github.lxgaming.analysis.common.integration.reconstruct.ReconstructIntegration;
import io.github.lxgaming.analysis.common.util.Toolbox;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class IntegrationManager {
    
    private static final Set<Integration> INTEGRATIONS = new LinkedHashSet<>();
    private static final Set<Class<? extends Integration>> INTEGRATION_CLASSES = new HashSet<>();
    
    public static void prepare() {
        registerIntegration(MinecraftIntegration.class);
        registerIntegration(ReconstructIntegration.class);
    }
    
    public static void execute() {
        for (Integration integration : INTEGRATIONS) {
            try {
                integration.execute();
            } catch (Exception ex) {
                Analysis.getInstance().getLogger().error("Encountered an error while executing {}", Toolbox.getClassSimpleName(integration.getClass()), ex);
            }
        }
    }
    
    public static void shutdown() {
        for (Integration integration : INTEGRATIONS) {
            try {
                integration.shutdown();
            } catch (Exception ex) {
                Analysis.getInstance().getLogger().error("Encountered an error while shutting down {}", Toolbox.getClassSimpleName(integration.getClass()), ex);
            }
        }
    }
    
    public static boolean registerIntegration(Class<? extends Integration> integrationClass) {
        if (INTEGRATION_CLASSES.contains(integrationClass)) {
            Analysis.getInstance().getLogger().warn("{} is already registered", Toolbox.getClassSimpleName(integrationClass));
            return false;
        }
        
        INTEGRATION_CLASSES.add(integrationClass);
        Integration integration = Toolbox.newInstance(integrationClass);
        if (integration == null) {
            Analysis.getInstance().getLogger().error("{} failed to initialize", Toolbox.getClassSimpleName(integrationClass));
            return false;
        }
        
        try {
            if (!integration.prepare()) {
                Analysis.getInstance().getLogger().warn("{} failed to prepare", Toolbox.getClassSimpleName(integrationClass));
                return false;
            }
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while preparing {}", Toolbox.getClassSimpleName(integrationClass), ex);
            return false;
        }
        
        INTEGRATIONS.add(integration);
        Analysis.getInstance().getLogger().debug("{} registered", Toolbox.getClassSimpleName(integrationClass));
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Integration> T getIntegration(Class<T> integrationClass) {
        for (Integration integration : INTEGRATIONS) {
            if (integration.getClass() == integrationClass) {
                return (T) integration;
            }
        }
        
        return null;
    }
}