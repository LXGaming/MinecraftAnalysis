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

package io.github.lxgaming.minecraftanalysis.client.configuration;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.beust.jcommander.converters.URLConverter;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class Config {
    
    @Parameter(names = {"-debug", "--debug"}, description = "For debugging purposes")
    private boolean debug = false;
    
    @Parameter(names = {"-query", "--query", "-queries", "--queries"}, description = "Queries to use during analysis")
    private List<String> queries = null;
    
    @Parameter(names = {"-input", "--input"}, description = "Input Path", required = true, converter = PathConverter.class)
    private Path inputPath = null;
    
    @Parameter(names = {"-output", "--output"}, description = "Output Path", converter = PathConverter.class)
    private Path outputPath = null;
    
    @Parameter(names = {"-address", "--address"}, description = "Address", converter = URLConverter.class)
    private URL address = null;
    
    public boolean isDebug() {
        return debug;
    }
    
    public List<String> getQueries() {
        return queries;
    }
    
    public Path getInputPath() {
        return inputPath;
    }
    
    public Path getOutputPath() {
        return outputPath;
    }
    
    public URL getAddress() {
        return address;
    }
}