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

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.github.lxgaming.analysis.common.util.gson.PackVersionAdapter;

@JsonAdapter(value = PackVersionAdapter.class)
public class PackVersion {
    
    @SerializedName("data")
    private int data;
    
    @SerializedName("resource")
    private int resource;
    
    private boolean legacy;
    
    public PackVersion(int value) {
        this(value, value, true);
    }
    
    public PackVersion(int data, int resource) {
        this(data, resource, false);
    }
    
    private PackVersion(int data, int resource, boolean legacy) {
        this.data = data;
        this.resource = resource;
        this.legacy = legacy;
    }
    
    public int getData() {
        return data;
    }
    
    public int getResource() {
        return resource;
    }
    
    public boolean isLegacy() {
        return legacy;
    }
}