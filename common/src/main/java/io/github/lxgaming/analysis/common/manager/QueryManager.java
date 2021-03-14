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

package io.github.lxgaming.analysis.common.manager;

import io.github.lxgaming.analysis.common.Analysis;
import io.github.lxgaming.analysis.common.query.PropertiesQuery;
import io.github.lxgaming.analysis.common.query.ProtocolQuery;
import io.github.lxgaming.analysis.common.query.Query;
import io.github.lxgaming.analysis.common.util.StringUtils;
import io.github.lxgaming.analysis.common.util.Toolbox;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class QueryManager {
    
    private static final Set<Query> QUERIES = new LinkedHashSet<>();
    private static final Set<Class<? extends Query>> QUERY_CLASSES = new HashSet<>();
    
    public static void prepare() {
        registerQuery(PropertiesQuery.class);
        registerQuery(ProtocolQuery.class);
    }
    
    public static void execute() {
        for (Query query : QUERIES) {
            try {
                query.execute();
            } catch (Exception ex) {
                Analysis.getInstance().getLogger().error("Encountered an error while executing {}", Toolbox.getClassSimpleName(query.getClass()), ex);
            }
        }
    }
    
    public static boolean registerAlias(Query query, String alias) {
        if (StringUtils.containsIgnoreCase(query.getAliases(), alias)) {
            Analysis.getInstance().getLogger().warn("{} is already registered for {}", alias, Toolbox.getClassSimpleName(query.getClass()));
            return false;
        }
        
        query.getAliases().add(alias);
        Analysis.getInstance().getLogger().debug("{} registered for {}", alias, Toolbox.getClassSimpleName(query.getClass()));
        return true;
    }
    
    public static boolean registerQuery(Class<? extends Query> queryClass) {
        if (QUERY_CLASSES.contains(queryClass)) {
            Analysis.getInstance().getLogger().warn("{} is already registered", Toolbox.getClassSimpleName(queryClass));
            return false;
        }
        
        QUERY_CLASSES.add(queryClass);
        Query query = Toolbox.newInstance(queryClass);
        if (query == null) {
            Analysis.getInstance().getLogger().error("{} failed to initialize", Toolbox.getClassSimpleName(queryClass));
            return false;
        }
        
        try {
            if (!query.prepare()) {
                Analysis.getInstance().getLogger().warn("{} failed to prepare", Toolbox.getClassSimpleName(queryClass));
                return false;
            }
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while preparing {}", Toolbox.getClassSimpleName(queryClass), ex);
            return false;
        }
        
        Collection<String> queries = Analysis.getInstance().getConfig().getQueries();
        if (queries != null && !queries.isEmpty() && !StringUtils.containsIgnoreCase(queries, query.getAliases())) {
            return false;
        }
        
        QUERIES.add(query);
        Analysis.getInstance().getLogger().debug("{} registered", Toolbox.getClassSimpleName(queryClass));
        return true;
    }
}