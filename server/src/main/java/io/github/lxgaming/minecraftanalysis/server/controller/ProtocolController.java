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

package io.github.lxgaming.minecraftanalysis.server.controller;

import io.github.lxgaming.minecraftanalysis.server.model.Protocol;
import io.github.lxgaming.minecraftanalysis.server.model.Version;
import io.github.lxgaming.minecraftanalysis.server.model.form.ProtocolForm;
import io.github.lxgaming.minecraftanalysis.server.repository.ProtocolRepository;
import io.github.lxgaming.minecraftanalysis.server.repository.VersionRepository;
import io.github.lxgaming.minecraftanalysis.server.util.Toolbox;
import io.github.lxgaming.minecraftanalysis.server.validation.ValidationSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/protocol/{version}")
public class ProtocolController {
    
    private final ProtocolRepository protocolRepository;
    private final VersionRepository versionRepository;
    
    @Autowired
    private ProtocolController(ProtocolRepository protocolRepository, VersionRepository versionRepository) {
        this.protocolRepository = protocolRepository;
        this.versionRepository = versionRepository;
    }
    
    @PostMapping
    public ResponseEntity<?> createProtocol(@PathVariable("version") String versionId,
                                            @RequestBody @Validated(ValidationSequence.class) List<ProtocolForm> protocolForms,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            if (bindingResult.hasFieldErrors()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Toolbox.getFieldErrors(bindingResult));
            }
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
        
        Version version = versionRepository.findById(versionId).orElse(null);
        if (version == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        for (ProtocolForm protocolForm : protocolForms) {
            Protocol protocol = new Protocol();
            protocol.setName(protocolForm.getName());
            protocol.setVersion(version);
            protocol.setPacket(protocolForm.getPacket());
            protocol.setDirection(protocolForm.getDirection());
            protocol.setState(protocolForm.getState());
            
            protocolRepository.save(protocol);
        }
        
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}