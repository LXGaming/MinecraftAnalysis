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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lxgaming.analysis.common.Analysis;
import io.github.lxgaming.analysis.common.configuration.ReconstructConfig;
import io.github.lxgaming.analysis.common.integration.Integration;
import io.github.lxgaming.analysis.common.integration.minecraft.MCPIntegration;
import io.github.lxgaming.analysis.common.integration.minecraft.YarnIntegration;
import io.github.lxgaming.analysis.common.manager.IntegrationManager;
import io.github.lxgaming.reconstruct.common.Reconstruct;
import io.github.lxgaming.reconstruct.common.bytecode.Attribute;
import io.github.lxgaming.reconstruct.common.bytecode.Attributes;
import io.github.lxgaming.reconstruct.common.bytecode.RcArray;
import io.github.lxgaming.reconstruct.common.bytecode.RcClass;
import io.github.lxgaming.reconstruct.common.bytecode.RcField;
import io.github.lxgaming.reconstruct.common.bytecode.RcMethod;
import io.github.lxgaming.reconstruct.common.util.Toolbox;
import net.fabricmc.mapping.tree.ClassDef;
import net.minecraftforge.srgutils.IMappingFile;

public class ReconstructIntegration extends Integration {
    
    private static final Attribute.Key<String> MCP_NAME = Attribute.Key.of("mcp_name", String.class);
    private static final Attribute.Key<String> YARN_NAME = Attribute.Key.of("yarn_name", String.class);
    
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
            
            applyMCPMapping();
            applyYarnMapping();
            writeMapping();
            return true;
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error during reconstruction", ex);
            return false;
        } finally {
            Reconstruct.getInstance().shutdown();
        }
    }
    
    private void applyMCPMapping() {
        MCPIntegration integration = IntegrationManager.getIntegration(MCPIntegration.class);
        if (integration == null) {
            return;
        }
        
        for (RcClass rcClass : Reconstruct.getInstance().getClasses()) {
            if (rcClass instanceof RcArray) {
                continue;
            }
            
            String className = rcClass.getAttribute(Attributes.OBFUSCATED_NAME).orElse(null);
            if (className == null || className.equals(rcClass.getName())) {
                continue;
            }
            
            IMappingFile.IClass mappingClass = integration.getMappingFile().getClass(className);
            if (mappingClass == null) {
                Analysis.getInstance().getLogger().warn("Unknown Class: {}", rcClass.getName());
                continue;
            }
            
            rcClass.setAttribute(MCP_NAME, Toolbox.toJvmName(mappingClass.getMapped()));
            Analysis.getInstance().getLogger().debug("MCP Class: {} -> {}", rcClass.getName(), Toolbox.toJvmName(mappingClass.getMapped()));
            
            for (RcField rcField : rcClass.getFields()) {
                String fieldName = rcField.getAttribute(Attributes.OBFUSCATED_NAME).orElse(null);
                if (fieldName == null || fieldName.equals(rcField.getName())) {
                    continue;
                }
                
                String name = mappingClass.remapField(fieldName);
                if (name == null || name.equals(fieldName)) {
                    Analysis.getInstance().getLogger().warn("Unknown Field: {} ({})", fieldName, rcField.getName());
                    continue;
                }
                
                rcField.setAttribute(MCP_NAME, name);
                Analysis.getInstance().getLogger().debug("MCP Field: {} -> {}", rcField.getName(), name);
            }
            
            for (RcMethod rcMethod : rcClass.getMethods()) {
                String descriptor = rcMethod.getAttribute(Attributes.OBFUSCATED_DESCRIPTOR).orElse(null);
                if (descriptor == null || descriptor.equals(rcMethod.getDescriptor())) {
                    continue;
                }
                
                int index = descriptor.indexOf('(');
                String methodName = descriptor.substring(0, index);
                String methodDesc = descriptor.substring(index);
                String name = mappingClass.remapMethod(methodName, methodDesc);
                if (name == null || name.equals(methodName)) {
                    Analysis.getInstance().getLogger().warn("Unknown Method: {} ({})", descriptor, rcMethod.getName());
                    continue;
                }
                
                rcMethod.setAttribute(MCP_NAME, name);
                Analysis.getInstance().getLogger().debug("MCP Method: {} -> {}", rcMethod.getName(), name);
            }
        }
    }
    
    private void applyYarnMapping() {
        YarnIntegration integration = IntegrationManager.getIntegration(YarnIntegration.class);
        if (integration == null) {
            return;
        }
        
        for (RcClass rcClass : Reconstruct.getInstance().getClasses()) {
            if (rcClass instanceof RcArray) {
                continue;
            }
            
            String className = rcClass.getAttribute(Attributes.OBFUSCATED_NAME).orElse(null);
            if (className == null || className.equals(rcClass.getName())) {
                continue;
            }
            
            ClassDef mappingClass = integration.getTinyTree().getDefaultNamespaceClassMap().get(className);
            if (mappingClass == null) {
                Analysis.getInstance().getLogger().warn("Unknown Class: {}", rcClass.getName());
                continue;
            }
            
            rcClass.setAttribute(YARN_NAME, Toolbox.toJvmName(mappingClass.getName("intermediary")));
            Analysis.getInstance().getLogger().debug("Yarn Class: {} -> {}", rcClass.getName(), Toolbox.toJvmName(mappingClass.getName("intermediary")));
            
            for (RcField rcField : rcClass.getFields()) {
                String fieldName = rcField.getAttribute(Attributes.OBFUSCATED_NAME).orElse(null);
                if (fieldName == null || fieldName.equals(rcField.getName())) {
                    continue;
                }
                
                String name = integration.getField(mappingClass, fieldName);
                if (name == null || name.equals(fieldName)) {
                    Analysis.getInstance().getLogger().warn("Unknown Field: {} ({})", fieldName, rcField.getName());
                    continue;
                }
                
                rcField.setAttribute(YARN_NAME, name);
                Analysis.getInstance().getLogger().debug("Yarn Field: {} -> {}", rcField.getName(), name);
            }
            
            for (RcMethod rcMethod : rcClass.getMethods()) {
                String descriptor = rcMethod.getAttribute(Attributes.OBFUSCATED_DESCRIPTOR).orElse(null);
                if (descriptor == null || descriptor.equals(rcMethod.getDescriptor())) {
                    continue;
                }
                
                int index = descriptor.indexOf('(');
                String methodName = descriptor.substring(0, index);
                String methodDesc = descriptor.substring(index);
                String name = integration.getMethod(mappingClass, methodName, methodDesc);
                if (name == null || name.equals(methodName)) {
                    Analysis.getInstance().getLogger().warn("Unknown Method: {} ({})", descriptor, rcMethod.getName());
                    continue;
                }
                
                rcMethod.setAttribute(YARN_NAME, name);
                Analysis.getInstance().getLogger().debug("Yarn Method: {} -> {}", rcMethod.getName(), name);
            }
        }
    }
    
    private void writeMapping() {
        JsonArray mapping = new JsonArray();
        for (RcClass rcClass : Reconstruct.getInstance().getClasses()) {
            if (rcClass instanceof RcArray) {
                continue;
            }
            
            JsonObject classMapping = createMapping(rcClass, rcClass.getName());
            if (classMapping == null) {
                continue;
            }
            
            mapping.add(classMapping);
            
            JsonArray fields = new JsonArray();
            for (RcField rcField : rcClass.getFields()) {
                JsonObject fieldMapping = createMapping(rcField, rcField.getName());
                if (fieldMapping == null) {
                    continue;
                }
                
                fields.add(fieldMapping);
            }
            
            if (fields.size() != 0) {
                classMapping.add("fields", fields);
            }
            
            JsonArray methods = new JsonArray();
            for (RcMethod rcMethod : rcClass.getMethods()) {
                JsonObject methodMapping = createMapping(rcMethod, rcMethod.getName());
                if (methodMapping == null) {
                    continue;
                }
                
                methods.add(methodMapping);
            }
            
            if (methods.size() != 0) {
                classMapping.add("methods", methods);
            }
        }
        
        Analysis.getInstance().write("mapping", mapping);
    }
    
    private JsonObject createMapping(Attributes attributes, String name) {
        String obfuscatedName = attributes.getAttribute(Attributes.OBFUSCATED_NAME).map(Toolbox::toJavaName).orElse(null);
        if (obfuscatedName == null || obfuscatedName.equals(name)) {
            return null;
        }
        
        String mcpName = attributes.getAttribute(MCP_NAME).map(Toolbox::toJavaName).orElse(null);
        String yarnName = attributes.getAttribute(YARN_NAME).map(Toolbox::toJavaName).orElse(null);
        
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mcp", mcpName);
        jsonObject.addProperty("mojang", name);
        jsonObject.addProperty("obfuscated", obfuscatedName);
        jsonObject.addProperty("yarn", yarnName);
        
        return jsonObject;
    }
    
    public ReconstructConfig getConfig() {
        return config;
    }
}