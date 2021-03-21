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

import java.util.Map;

public class Downloads {
    
    @SerializedName("artifact")
    private Artifact artifact;
    
    @SerializedName("classifiers")
    private Map<String, Artifact> classifiers;
    
    public Artifact getArtifact() {
        return artifact;
    }
    
    public Map<String, Artifact> getClassifiers() {
        return classifiers;
    }
}