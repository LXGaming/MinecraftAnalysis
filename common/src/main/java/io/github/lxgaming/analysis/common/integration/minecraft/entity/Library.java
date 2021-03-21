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
        if (getNatives() == null) {
            return null;
        }
        
        String name = System.getProperty("os.name");
        if (StringUtils.containsIgnoreCase(name, "BSD") || StringUtils.containsIgnoreCase(name, "Linux") || StringUtils.containsIgnoreCase(name, "Unix")) {
            return getDownloads().getClassifiers().get(getNatives().getLinux());
        }
        
        if (StringUtils.startsWithIgnoreCase(name, "Mac OS")) {
            return getDownloads().getClassifiers().get(getNatives().getOsx());
        }
        
        if (StringUtils.startsWithIgnoreCase(name, "Windows")) {
            return getDownloads().getClassifiers().get(getNatives().getWindows());
        }
        
        throw new IllegalStateException(String.format("%s is not supported", name));
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