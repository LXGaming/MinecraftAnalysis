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

package io.github.lxgaming.minecraftanalysis.client.entity;

import com.google.gson.annotations.SerializedName;

public class Version {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName(value = "releaseTarget", alternate = "release_target")
    private String releaseTarget;
    
    @SerializedName(value = "worldVersion", alternate = "world_version")
    private Integer worldVersion;
    
    @SerializedName(value = "protocolVersion", alternate = "protocol_version")
    private Integer protocolVersion;
    
    @SerializedName(value = "packVersion", alternate = "pack_version")
    private Integer packVersion;
    
    @SerializedName(value = "buildTime", alternate = "build_time")
    private String buildTime;
    
    @SerializedName("stable")
    private Boolean stable;
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getReleaseTarget() {
        return releaseTarget;
    }
    
    public Integer getWorldVersion() {
        return worldVersion;
    }
    
    public Integer getProtocolVersion() {
        return protocolVersion;
    }
    
    public Integer getPackVersion() {
        return packVersion;
    }
    
    public String getBuildTime() {
        return buildTime;
    }
    
    public Boolean getStable() {
        return stable;
    }
}