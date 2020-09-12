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

package io.github.lxgaming.minecraftanalysis.client;

import com.beust.jcommander.JCommander;
import io.github.lxgaming.minecraftanalysis.client.util.ShutdownHook;
import org.apache.logging.log4j.core.config.Configurator;

public class Main {
    
    public static void main(String[] args) {
        Thread.currentThread().setName("Main Thread");
        MinecraftAnalysis analysis = new MinecraftAnalysis();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        
        MinecraftAnalysis.getInstance().getLogger().info("{} v{}", MinecraftAnalysis.NAME, MinecraftAnalysis.VERSION);
        MinecraftAnalysis.getInstance().getLogger().info("Authors: {}", MinecraftAnalysis.AUTHORS);
        MinecraftAnalysis.getInstance().getLogger().info("Source: {}", MinecraftAnalysis.SOURCE);
        MinecraftAnalysis.getInstance().getLogger().info("Website: {}", MinecraftAnalysis.WEBSITE);
        
        try {
            JCommander.newBuilder()
                    .addObject(MinecraftAnalysis.getInstance().getConfig())
                    .build()
                    .parse(args);
        } catch (Exception ex) {
            MinecraftAnalysis.getInstance().getLogger().error("Encountered an error while parsing arguments", ex);
            Runtime.getRuntime().exit(-1);
            return;
        }
        
        if (MinecraftAnalysis.getInstance().getConfig().isDebug()) {
            System.setProperty("minecraftanalysis.logging.console.level", "DEBUG");
            Configurator.reconfigure();
            MinecraftAnalysis.getInstance().getLogger().debug("Debug mode enabled");
        } else {
            MinecraftAnalysis.getInstance().getLogger().info("Debug mode disabled");
        }
        
        analysis.load();
    }
}