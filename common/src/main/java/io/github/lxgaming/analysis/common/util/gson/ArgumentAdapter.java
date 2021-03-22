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

package io.github.lxgaming.analysis.common.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.lxgaming.analysis.common.integration.minecraft.entity.Argument;
import io.github.lxgaming.analysis.common.integration.minecraft.entity.Rule;
import io.github.lxgaming.analysis.common.util.Toolbox;

import java.io.IOException;

public class ArgumentAdapter extends TypeAdapter<Argument> {
    
    @Override
    public void write(JsonWriter out, Argument value) throws IOException {
        if (value.getRules() == null && value.getValue().size() == 1) {
            out.value(value.getValue().get(0));
            return;
        }
        
        out.beginObject();
        
        out.name("rules");
        Toolbox.GSON.toJson(value.getRules(), Rule[].class, out);
        
        out.name("value");
        if (value.getValue().size() == 1) {
            String stringValue = value.getValue().get(0);
            
            // Handle inconsistencies
            if (stringValue.equals("-XstartOnFirstThread")) {
                Toolbox.GSON.toJson(value.getValue(), String[].class, out);
            } else {
                out.value(stringValue);
            }
        } else {
            Toolbox.GSON.toJson(value.getValue(), String[].class, out);
        }
        
        out.endObject();
    }
    
    @Override
    public Argument read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.STRING) {
            String value = in.nextString();
            return new Argument(value);
        }
        
        if (in.peek() == JsonToken.BEGIN_OBJECT) {
            in.beginObject();
            
            Argument argument = new Argument();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("rules")) {
                    argument.getRules().addAll(Toolbox.GSON.fromJson(in, Rule[].class));
                    continue;
                }
                
                if (name.equals("value")) {
                    if (in.peek() == JsonToken.BEGIN_ARRAY) {
                        argument.getValue().addAll(Toolbox.GSON.fromJson(in, String[].class));
                        continue;
                    }
                    
                    if (in.peek() == JsonToken.STRING) {
                        argument.getValue().add(in.nextString());
                    }
                }
            }
            
            in.endObject();
            return argument;
        }
        
        return null;
    }
}