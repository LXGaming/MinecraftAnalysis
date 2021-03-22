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
import io.github.lxgaming.analysis.common.entity.OSPlatform;

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
        
        OSPlatform platform = OSPlatform.getOSPlatform();
        if (platform.isLinux()) {
            return getName().equals("linux");
        } else if (platform.isMacOS()) {
            return getName().equals("osx");
        } else if (platform.isWindows()) {
            return getName().equals("windows");
        } else {
            throw new IllegalStateException(String.format("%s is not supported", platform));
        }
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