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

package io.github.lxgaming.analysis.common.query;

import io.github.lxgaming.analysis.common.Analysis;
import io.github.lxgaming.analysis.common.manager.QueryManager;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Query {
    
    private final Set<String> aliases = new LinkedHashSet<>();
    
    public abstract boolean prepare();
    
    public abstract void execute() throws Exception;
    
    protected final Class<?> loadClass(String name) throws ClassNotFoundException {
        return Analysis.getInstance().getClassLoader().loadClass(name);
    }
    
    protected final void addAlias(String alias) {
        QueryManager.registerAlias(this, alias);
    }
    
    public final Set<String> getAliases() {
        return aliases;
    }
}