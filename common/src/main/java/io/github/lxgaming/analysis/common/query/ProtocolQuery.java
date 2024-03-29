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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lxgaming.analysis.common.Analysis;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;

public class ProtocolQuery extends Query {
    
    @Override
    public boolean prepare() {
        addAlias("protocol");
        return true;
    }
    
    @Override
    public void execute() throws Exception {
        JsonArray jsonArray;
        if (Analysis.getInstance().getManifest().getId().equals("1.14.4")) {
            jsonArray = processPackets();
        } else {
            jsonArray = processFlows();
        }
        
        Analysis.getInstance().write("protocol", jsonArray);
    }
    
    // 1.15+
    @SuppressWarnings("unchecked")
    private JsonArray processFlows() throws Exception {
        Class<?> connectionProtocolClass = loadClass("net.minecraft.network.ConnectionProtocol");
        
        // net.minecraft.network.ConnectionProtocol.HANDSHAKING
        Field handshakeField = connectionProtocolClass.getDeclaredField("HANDSHAKING");
        
        // net.minecraft.network.ConnectionProtocol.STATUS
        Field statusField = connectionProtocolClass.getDeclaredField("STATUS");
        
        // net.minecraft.network.ConnectionProtocol.LOGIN
        Field loginField = connectionProtocolClass.getDeclaredField("LOGIN");
        
        // net.minecraft.network.ConnectionProtocol.PLAY
        Field playField = connectionProtocolClass.getDeclaredField("PLAY");
        
        // net.minecraft.network.ConnectionProtocol.flows
        Field flowsField = connectionProtocolClass.getDeclaredField("flows");
        flowsField.setAccessible(true);
        
        Class<?> packetSetClass = loadClass("net.minecraft.network.ConnectionProtocol$PacketSet");
        
        // net.minecraft.network.ConnectionProtocol.PacketSet.classToId
        Field classToIdField = packetSetClass.getDeclaredField("classToId");
        classToIdField.setAccessible(true);
        
        Object handshakeProtocol = handshakeField.get(null);
        Object statusProtocol = statusField.get(null);
        Object loginProtocol = loginField.get(null);
        Object playProtocol = playField.get(null);
        
        JsonArray jsonArray = new JsonArray();
        for (Object connectionProtocol : new Object[]{handshakeProtocol, statusProtocol, loginProtocol, playProtocol}) {
            
            Map<Object, Object> flows = (Map<Object, Object>) flowsField.get(connectionProtocol);
            for (Map.Entry<Object, Object> flowEntry : flows.entrySet()) {
                
                Map<Class<?>, Integer> classToId = (Map<Class<?>, Integer>) classToIdField.get(flowEntry.getValue());
                classToId.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).forEach(entry -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", entry.getValue());
                    jsonObject.addProperty("name", entry.getKey().getName());
                    jsonObject.addProperty("direction", flowEntry.getKey().toString());
                    jsonObject.addProperty("state", connectionProtocol.toString());
                    jsonArray.add(jsonObject);
                });
            }
        }
        
        return jsonArray;
    }
    
    // 1.14.4
    @SuppressWarnings("unchecked")
    private JsonArray processPackets() throws Exception {
        Class<?> connectionProtocolClass = loadClass("net.minecraft.network.ConnectionProtocol");
        
        // net.minecraft.network.ConnectionProtocol.HANDSHAKING
        Field handshakeField = connectionProtocolClass.getDeclaredField("HANDSHAKING");
        
        // net.minecraft.network.ConnectionProtocol.STATUS
        Field statusField = connectionProtocolClass.getDeclaredField("STATUS");
        
        // net.minecraft.network.ConnectionProtocol.LOGIN
        Field loginField = connectionProtocolClass.getDeclaredField("LOGIN");
        
        // net.minecraft.network.ConnectionProtocol.PLAY
        Field playField = connectionProtocolClass.getDeclaredField("PLAY");
        
        // net.minecraft.network.ConnectionProtocol.flows
        Field packetsField = connectionProtocolClass.getDeclaredField("packets");
        packetsField.setAccessible(true);
        
        Object handshakeProtocol = handshakeField.get(null);
        Object statusProtocol = statusField.get(null);
        Object loginProtocol = loginField.get(null);
        Object playProtocol = playField.get(null);
        
        JsonArray jsonArray = new JsonArray();
        for (Object connectionProtocol : new Object[]{handshakeProtocol, statusProtocol, loginProtocol, playProtocol}) {
            
            Map<Object, Map<Integer, Class<?>>> packets = (Map<Object, Map<Integer, Class<?>>>) packetsField.get(connectionProtocol);
            for (Map.Entry<Object, Map<Integer, Class<?>>> packetEntry : packets.entrySet()) {
                
                for (Map.Entry<Integer, Class<?>> entry : packetEntry.getValue().entrySet()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", entry.getKey());
                    jsonObject.addProperty("name", entry.getValue().getName());
                    jsonObject.addProperty("direction", packetEntry.getKey().toString());
                    jsonObject.addProperty("state", connectionProtocol.toString());
                    jsonArray.add(jsonObject);
                }
            }
        }
        
        return jsonArray;
    }
}