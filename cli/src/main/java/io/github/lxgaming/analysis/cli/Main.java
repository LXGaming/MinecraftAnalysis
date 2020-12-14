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

package io.github.lxgaming.analysis.cli;

import com.beust.jcommander.JCommander;
import io.github.lxgaming.analysis.cli.configuration.ConfigImpl;
import io.github.lxgaming.analysis.cli.util.ShutdownHook;
import io.github.lxgaming.analysis.common.Analysis;
import org.apache.logging.log4j.core.config.Configurator;

public class Main {
    
    public static void main(String[] args) {
        Thread.currentThread().setName("Main Thread");
        Analysis analysis = new Analysis(new ConfigImpl());
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        
        Analysis.getInstance().getLogger().info("{} v{}", Analysis.NAME, Analysis.VERSION);
        Analysis.getInstance().getLogger().info("Authors: {}", Analysis.AUTHORS);
        Analysis.getInstance().getLogger().info("Source: {}", Analysis.SOURCE);
        Analysis.getInstance().getLogger().info("Website: {}", Analysis.WEBSITE);
        
        try {
            JCommander.newBuilder()
                    .addObject(Analysis.getInstance().getConfig())
                    .build()
                    .parse(args);
        } catch (Exception ex) {
            Analysis.getInstance().getLogger().error("Encountered an error while parsing arguments", ex);
            Runtime.getRuntime().exit(-1);
            return;
        }
        
        if (Analysis.getInstance().getConfig().isDebug()) {
            System.setProperty("analysis.logging.console.level", "DEBUG");
            Configurator.reconfigure();
            Analysis.getInstance().getLogger().debug("Debug mode enabled");
        } else {
            Analysis.getInstance().getLogger().info("Debug mode disabled");
        }
        
        analysis.load();
    }
}