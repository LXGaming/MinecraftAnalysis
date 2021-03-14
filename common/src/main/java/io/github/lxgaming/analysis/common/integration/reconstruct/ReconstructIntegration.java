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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
            Path outputPath = config.getOutputPath();
            Path temporaryOutputPath = outputPath.resolveSibling(outputPath.getFileName().toString() + ".tmp");
            config.setOutputPath(temporaryOutputPath);
            
            Reconstruct reconstruct = new Reconstruct(config);
            reconstruct.load();
            
            config.setOutputPath(outputPath);
            Files.move(temporaryOutputPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
            
            applyMappings();
            writeMappings();
            return true;
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error during reconstruction", ex);
            return false;
        } finally {
            Reconstruct.getInstance().shutdown();
        }
    }
    
    private void applyMappings() {
        MCPIntegration mcpIntegration = IntegrationManager.getIntegration(MCPIntegration.class);
        YarnIntegration yarnIntegration = IntegrationManager.getIntegration(YarnIntegration.class);
        
        for (RcClass rcClass : Reconstruct.getInstance().getClasses()) {
            if (rcClass instanceof RcArray) {
                continue;
            }
            
            if (rcClass.getName().endsWith(".package-info")) {
                continue;
            }
            
            String obfuscatedClassName = rcClass.getAttribute(Attributes.OBFUSCATED_NAME).orElse(rcClass.getName());
            if (obfuscatedClassName.equals(rcClass.getName()) && !rcClass.getName().startsWith("net.minecraft.")) {
                continue;
            }
            
            IMappingFile.IClass mcpClass;
            if (mcpIntegration != null && mcpIntegration.getMappingFile() != null) {
                mcpClass = mcpIntegration.getMappingFile().getClass(Toolbox.toJvmName(obfuscatedClassName));
                if (mcpClass != null) {
                    String mcpClassName = Toolbox.toJvmName(mcpClass.getMapped());
                    rcClass.setAttribute(MCP_NAME, mcpClassName);
                    Analysis.getInstance().getLogger().debug("MCP Class: {} -> {}", rcClass.getName(), mcpClassName);
                } else {
                    Analysis.getInstance().getLogger().warn("Unknown MCP Class: {}", rcClass.getName());
                }
            } else {
                mcpClass = null;
            }
            
            ClassDef yarnClass;
            if (yarnIntegration != null && yarnIntegration.getTinyTree() != null) {
                yarnClass = yarnIntegration.getTinyTree().getDefaultNamespaceClassMap().get(Toolbox.toJvmName(obfuscatedClassName));
                if (yarnClass != null) {
                    String yarnClassName = Toolbox.toJvmName(yarnClass.getName("intermediary"));
                    rcClass.setAttribute(YARN_NAME, yarnClassName);
                    Analysis.getInstance().getLogger().debug("Yarn Class: {} -> {}", rcClass.getName(), yarnClassName);
                } else {
                    Analysis.getInstance().getLogger().warn("Unknown Yarn Class: {}", rcClass.getName());
                }
            } else {
                yarnClass = null;
            }
            
            if (mcpClass == null && yarnClass == null) {
                continue;
            }
            
            for (RcField rcField : rcClass.getFields()) {
                String obfuscatedFieldName = rcField.getAttribute(Attributes.OBFUSCATED_NAME).orElse(null);
                if (obfuscatedFieldName == null || obfuscatedFieldName.equals(rcField.getName())) {
                    continue;
                }
                
                if (mcpClass != null) {
                    String mcpFieldName = mcpClass.remapField(obfuscatedFieldName);
                    if (mcpFieldName != null && !mcpFieldName.equals(obfuscatedFieldName)) {
                        rcField.setAttribute(MCP_NAME, mcpFieldName);
                        Analysis.getInstance().getLogger().debug("MCP Field: {} -> {}", rcField.getName(), mcpFieldName);
                    } else {
                        Analysis.getInstance().getLogger().warn("Unknown MCP Field: {}", rcField.getName());
                    }
                }
                
                if (yarnClass != null) {
                    String yarnFieldName = yarnIntegration.getField(yarnClass, obfuscatedFieldName);
                    if (yarnFieldName != null && !yarnFieldName.equals(obfuscatedFieldName)) {
                        rcField.setAttribute(YARN_NAME, yarnFieldName);
                        Analysis.getInstance().getLogger().debug("Yarn Field: {} -> {}", rcField.getName(), yarnFieldName);
                    } else {
                        Analysis.getInstance().getLogger().warn("Unknown Yarn Field: {}", rcField.getName());
                    }
                }
            }
            
            for (RcMethod rcMethod : rcClass.getMethods()) {
                String obfuscatedMethodDescriptor = rcMethod.getAttribute(Attributes.OBFUSCATED_DESCRIPTOR).orElse(null);
                if (obfuscatedMethodDescriptor == null || obfuscatedMethodDescriptor.equals(rcMethod.getDescriptor())) {
                    continue;
                }
                
                int index = obfuscatedMethodDescriptor.indexOf('(');
                if (index == -1) {
                    continue;
                }
                
                String obfuscatedMethodName = obfuscatedMethodDescriptor.substring(0, index);
                String obfuscatedMethodDesc = obfuscatedMethodDescriptor.substring(index);
                
                if (mcpClass != null) {
                    String mcpMethodName = mcpClass.remapMethod(obfuscatedMethodName, obfuscatedMethodDesc);
                    if (mcpMethodName != null && !mcpMethodName.equals(obfuscatedMethodName)) {
                        rcMethod.setAttribute(MCP_NAME, mcpMethodName);
                        Analysis.getInstance().getLogger().debug("MCP Method: {} -> {}", rcMethod.getName(), mcpMethodName);
                    } else {
                        Analysis.getInstance().getLogger().warn("Unknown MCP Method: {}", rcMethod.getName());
                    }
                }
                
                if (yarnClass != null) {
                    String yarnMethodName = yarnIntegration.getMethod(yarnClass, obfuscatedMethodName, obfuscatedMethodDesc);
                    if (yarnMethodName != null && !yarnMethodName.equals(obfuscatedMethodName)) {
                        rcMethod.setAttribute(YARN_NAME, yarnMethodName);
                        Analysis.getInstance().getLogger().debug("Yarn Method: {} -> {}", rcMethod.getName(), yarnMethodName);
                    } else {
                        Analysis.getInstance().getLogger().warn("Unknown Yarn Method: {}", rcMethod.getName());
                    }
                }
            }
        }
    }
    
    private void writeMappings() {
        JsonArray mapping = new JsonArray();
        for (RcClass rcClass : Reconstruct.getInstance().getClasses()) {
            if (rcClass instanceof RcArray) {
                continue;
            }
            
            if (rcClass.getName().endsWith(".package-info")) {
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
        String obfuscatedName = attributes.getAttribute(Attributes.OBFUSCATED_NAME).orElse(name);
        if (obfuscatedName.equals(name) && !name.startsWith("net.minecraft.")) {
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