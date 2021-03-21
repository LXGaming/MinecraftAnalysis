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

package io.github.lxgaming.analysis.common.integration.minecraft.entity;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class VersionManifest {
    
    // TODO Implement?
    @SerializedName("arguments")
    private JsonObject arguments;
    
    @SerializedName("assetIndex")
    private Artifact assetIndex;
    
    @SerializedName("assets")
    private String assets;
    
    @SerializedName("complianceLevel")
    private int complianceLevel;
    
    @SerializedName("downloads")
    private Map<String, Artifact> downloads;
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("libraries")
    private List<Library> libraries;
    
    // TODO Implement?
    @SerializedName("logging")
    private JsonObject logging;
    
    @SerializedName("mainClass")
    private String mainClass;
    
    @SerializedName("minimumLauncherVersion")
    private int minimumLauncherVersion;
    
    @SerializedName("releaseTime")
    private String releaseTime;
    
    @SerializedName("time")
    private String time;
    
    @SerializedName("type")
    private String type;
    
    public JsonObject getArguments() {
        return arguments;
    }
    
    public Artifact getAssetIndex() {
        return assetIndex;
    }
    
    public String getAssets() {
        return assets;
    }
    
    public int getComplianceLevel() {
        return complianceLevel;
    }
    
    public Map<String, Artifact> getDownloads() {
        return downloads;
    }
    
    public String getId() {
        return id;
    }
    
    public List<Library> getLibraries() {
        return libraries;
    }
    
    public JsonObject getLogging() {
        return logging;
    }
    
    public String getMainClass() {
        return mainClass;
    }
    
    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }
    
    public String getReleaseTime() {
        return releaseTime;
    }
    
    public String getTime() {
        return time;
    }
    
    public String getType() {
        return type;
    }
}