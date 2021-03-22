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

package io.github.lxgaming.analysis.common.entity;

import io.github.lxgaming.analysis.common.util.StringUtils;

public enum OSPlatform {
    
    LINUX("Linux"),
    MACOS("macOS"),
    WINDOWS("Windows");
    
    private final String name;
    
    OSPlatform(String name) {
        this.name = name;
    }
    
    public static OSPlatform getOSPlatform() {
        String name = System.getProperty("os.name");
        if (StringUtils.containsIgnoreCase(name, "BSD") || StringUtils.containsIgnoreCase(name, "Linux") || StringUtils.containsIgnoreCase(name, "Unix")) {
            return LINUX;
        }
        
        if (StringUtils.startsWithIgnoreCase(name, "Mac OS")) {
            return MACOS;
        }
        
        if (StringUtils.startsWithIgnoreCase(name, "Windows")) {
            return WINDOWS;
        }
        
        throw new IllegalStateException(String.format("%s is not supported", name));
    }
    
    public boolean isLinux() {
        return this == LINUX;
    }
    
    public boolean isMacOS() {
        return this == MACOS;
    }
    
    public boolean isWindows() {
        return this == WINDOWS;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}