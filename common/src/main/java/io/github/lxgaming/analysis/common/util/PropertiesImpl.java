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

package io.github.lxgaming.analysis.common.util;

import com.google.gson.JsonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class PropertiesImpl extends Properties {
    
    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<>(super.keySet()));
    }
    
    @Override
    public synchronized Object get(Object key) {
        Object object = super.get(key);
        if (object == null) {
            put(key, JsonNull.INSTANCE);
        }
        
        return object;
    }
    
    @Override
    public Set<Object> keySet() {
        return new TreeSet<>(super.keySet());
    }
    
    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        Set<Map.Entry<Object, Object>> set = new TreeSet<>(Comparator.comparing(string -> string.getKey().toString()));
        set.addAll(super.entrySet());
        return set;
    }
}