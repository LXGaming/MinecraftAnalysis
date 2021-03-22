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

import java.util.List;

public class Library {
    
    @SerializedName("downloads")
    private Downloads downloads;
    
    @SerializedName("extract")
    private Extract extract;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("natives")
    private Natives natives;
    
    @SerializedName("rules")
    public List<Rule> rules;
    
    public Artifact getNative() {
        if (getDownloads().getClassifiers() == null || getNatives() == null) {
            return null;
        }
        
        OSPlatform platform = OSPlatform.getOSPlatform();
        if (platform.isLinux()) {
            return getDownloads().getClassifiers().get(getNatives().getLinux());
        } else if (platform.isMacOS()) {
            return getDownloads().getClassifiers().get(getNatives().getOsx());
        } else if (platform.isWindows()) {
            return getDownloads().getClassifiers().get(getNatives().getWindows());
        } else {
            throw new IllegalStateException(String.format("%s is not supported", platform));
        }
    }
    
    public Downloads getDownloads() {
        return downloads;
    }
    
    public Extract getExtract() {
        return extract;
    }
    
    public String getName() {
        return name;
    }
    
    public Natives getNatives() {
        return natives;
    }
    
    public List<Rule> getRules() {
        return rules;
    }
}