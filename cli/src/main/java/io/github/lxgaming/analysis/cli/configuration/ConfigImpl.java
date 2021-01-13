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

package io.github.lxgaming.analysis.cli.configuration;

import com.beust.jcommander.Parameter;
import io.github.lxgaming.analysis.common.configuration.Config;

import java.util.List;

public class ConfigImpl implements Config {
    
    @Parameter(names = {"-debug", "--debug"}, description = "For debugging purposes")
    private boolean debug = false;
    
    @Parameter(names = {"-reconstruct", "--reconstruct"}, description = "Force Reconstruct")
    private boolean reconstruct = false;
    
    @Parameter(names = {"-thread", "--thread", "-threads", "--threads"}, description = "Performs deobfuscation asynchronously across the specified number of threads")
    private int threads = 0;
    
    @Parameter(names = {"-query", "--query", "-queries", "--queries"}, description = "Queries to use during analysis")
    private List<String> queries = null;
    
    @Parameter(names = {"-type", "--type"}, description = "Client or Server")
    private String type = "server";
    
    @Parameter(names = {"-version", "--version"}, description = "Target Minecraft Version")
    private String version = null;
    
    @Override
    public boolean isDebug() {
        return debug;
    }
    
    @Override
    public boolean isReconstruct() {
        return reconstruct;
    }
    
    @Override
    public void setReconstruct(boolean reconstruct) {
        this.reconstruct = reconstruct;
    }
    
    @Override
    public int getThreads() {
        return threads;
    }
    
    @Override
    public void setThreads(int threads) {
        this.threads = threads;
    }
    
    @Override
    public List<String> getQueries() {
        return queries;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public String getVersion() {
        return version;
    }
}