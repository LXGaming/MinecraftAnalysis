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

import com.google.gson.annotations.SerializedName;

public class Artifact {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("path")
    private String path;
    
    @SerializedName("sha1")
    private String hash;
    
    @SerializedName("size")
    private long size;
    
    @SerializedName("totalSize")
    private long totalSize;
    
    @SerializedName("url")
    private String url;
    
    public String getId() {
        return id;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getHash() {
        return hash;
    }
    
    public long getSize() {
        return size;
    }
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public String getUrl() {
        return url;
    }
}