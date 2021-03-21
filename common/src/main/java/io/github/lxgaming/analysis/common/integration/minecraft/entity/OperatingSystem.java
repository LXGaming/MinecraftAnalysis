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

package io.github.lxgaming.analysis.common.integration.minecraft.entity;

import com.google.gson.annotations.SerializedName;
import io.github.lxgaming.analysis.common.util.StringUtils;

public class OperatingSystem {
    
    @SerializedName("arch")
    private String architecture;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("version")
    private String version;
    
    public boolean isSupported() {
        return isArchitectureSupported() && isNameSupported() && isVersionSupported();
    }
    
    public boolean isArchitectureSupported() {
        if (getArchitecture() == null) {
            return true;
        }
        
        String architecture = System.getProperty("os.arch");
        if (getArchitecture().equals(architecture)) {
            return true;
        }
        
        throw new IllegalStateException(String.format("%s is not supported", architecture));
    }
    
    public boolean isNameSupported() {
        if (getName() == null) {
            return true;
        }
        
        String name = System.getProperty("os.name");
        if (StringUtils.containsIgnoreCase(name, "BSD") || StringUtils.containsIgnoreCase(name, "Linux") || StringUtils.containsIgnoreCase(name, "Unix")) {
            return getName().equals("linux");
        }
        
        if (StringUtils.startsWithIgnoreCase(name, "Mac OS")) {
            return getName().equals("osx");
        }
        
        if (StringUtils.startsWithIgnoreCase(name, "Windows")) {
            return getName().equals("windows");
        }
        
        throw new IllegalStateException(String.format("%s is not supported", name));
    }
    
    public boolean isVersionSupported() {
        if (getVersion() == null) {
            return true;
        }
        
        String version = System.getProperty("os.version");
        return version.matches(getVersion());
    }
    
    public String getArchitecture() {
        return architecture;
    }
    
    public String getName() {
        return name;
    }
    
    public String getVersion() {
        return version;
    }
}