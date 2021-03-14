/*
 * Copyright 2021 Alex Thomson
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

package io.github.lxgaming.analysis.common.query;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.lxgaming.analysis.common.Analysis;
import io.github.lxgaming.analysis.common.util.PropertiesImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

public class PropertiesQuery extends Query {
    
    @Override
    public boolean prepare() {
        addAlias("properties");
        return true;
    }
    
    @Override
    public void execute() throws Exception {
        Class<?> dedicatedServerPropertiesClass = Class.forName("net.minecraft.server.dedicated.DedicatedServerProperties");
        Constructor<?> dedicatedServerPropertiesConstructor = dedicatedServerPropertiesClass.getConstructors()[0];
        
        Properties properties = new PropertiesImpl();
        if (dedicatedServerPropertiesConstructor.getParameterCount() == 1) {
            dedicatedServerPropertiesConstructor.newInstance(properties);
        } else if (dedicatedServerPropertiesConstructor.getParameterCount() == 2) {
            Class<?> registryAccessClass = Class.forName("net.minecraft.core.RegistryAccess");
            
            // net.minecraft.core.RegistryAccess.builtin()
            Method method = registryAccessClass.getMethod("builtin");
            Object registryAccessInstance = method.invoke(null);
            
            dedicatedServerPropertiesConstructor.newInstance(properties, registryAccessInstance);
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported Constructor for %s", dedicatedServerPropertiesClass.getName()));
        }
        
        JsonObject settings = new JsonObject();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            
            JsonObject setting = new JsonObject();
            setting.addProperty("prop", key);
            
            if (value == null) {
                setting.add("value", JsonNull.INSTANCE);
            } else if (value instanceof String) {
                setting.addProperty("value", (String) value);
            } else if (value instanceof Number) {
                setting.addProperty("value", (Number) value);
            } else if (value instanceof Boolean) {
                setting.addProperty("value", (Boolean) value);
            } else if (value instanceof Character) {
                setting.addProperty("value", (Character) value);
            } else {
                Analysis.getInstance().getLogger().warn("{} is not supported ({})", value.getClass().getName(), key);
            }
            
            settings.add(key.replace(".", "_").replace("-", "_").toUpperCase(), setting);
        }
        
        Analysis.getInstance().write("properties", settings);
    }
}