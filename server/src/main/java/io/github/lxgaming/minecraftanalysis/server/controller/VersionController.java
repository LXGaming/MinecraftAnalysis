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

import io.github.lxgaming.minecraftanalysis.server.model.Version;
import io.github.lxgaming.minecraftanalysis.server.model.form.VersionForm;
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

@Controller
@RequestMapping("/version/{version}")
public class VersionController {
    
    private final VersionRepository versionRepository;
    
    @Autowired
    private VersionController(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }
    
    @PostMapping
    public ResponseEntity<?> createVersion(@PathVariable("version") String versionId,
                                           @RequestBody @Validated(ValidationSequence.class) VersionForm versionForm,
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
        
        if (versionRepository.existsById(versionForm.getId())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
        
        Version version = new Version();
        version.setId(versionForm.getId());
        version.setName(versionForm.getName());
        version.setReleaseTarget(versionForm.getReleaseTarget());
        version.setWorldVersion(versionForm.getWorldVersion());
        version.setProtocolVersion(versionForm.getProtocolVersion());
        version.setPackVersion(versionForm.getPackVersion());
        version.setBuildTime(versionForm.getBuildTime());
        version.setStable(versionForm.getStable());
        
        versionRepository.save(version);
        
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}