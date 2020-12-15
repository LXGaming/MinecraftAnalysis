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

package io.github.lxgaming.analysis.common.integration.minecraft;

import io.github.lxgaming.analysis.common.Analysis;
import io.github.lxgaming.analysis.common.integration.Integration;
import io.github.lxgaming.analysis.common.util.WebUtils;
import net.minecraftforge.srgutils.IMappingFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class MCPIntegration extends Integration {
    
    private URL url;
    private IMappingFile mappingFile;
    
    @Override
    public boolean prepare() {
        try {
            this.url = new URL(String.format(
                    "https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/versions/release/%s/joined.tsrg",
                    Analysis.getInstance().getConfig().getVersion()));
            
            return true;
        } catch (MalformedURLException ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while parsing url", ex);
            return false;
        }
    }
    
    @Override
    public void execute() throws Exception {
        Path mappingPath = Analysis.getInstance().getVersionPath().resolve("mapping.tsrg");
        
        if (!Files.exists(mappingPath)) {
            try {
                Analysis.getInstance().getLogger().debug("Downloading {}", url);
                WebUtils.downloadFile(url, mappingPath);
                Analysis.getInstance().getLogger().info("Downloaded MCP Mappings");
            } catch (Exception ex) {
                Analysis.getInstance().getLogger().error("Encountered an error while downloading {}", url, ex);
                return;
            }
        }
        
        this.mappingFile = IMappingFile.load(mappingPath.toFile());
    }
    
    @Override
    public void shutdown() {
        // no-op
    }
    
    public IMappingFile getMappingFile() {
        return mappingFile;
    }
}