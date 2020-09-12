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

package io.github.lxgaming.minecraftanalysis.server.model.form;

import io.github.lxgaming.minecraftanalysis.server.validation.ValidationSequence;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class VersionForm {
    
    @NotBlank(
            message = "{version.id.required}",
            groups = ValidationSequence.NotBlankGroup.class
    )
    private String id;
    
    @NotBlank(
            message = "{version.name.required}",
            groups = ValidationSequence.NotBlankGroup.class
    )
    private String name;
    
    @NotBlank(
            message = "{version.release_target.required}",
            groups = ValidationSequence.NotBlankGroup.class
    )
    private String releaseTarget;
    
    @NotNull(
            message = "{version.world_version.required}",
            groups = ValidationSequence.NotBlankGroup.class
    )
    private Integer worldVersion;
    
    @NotNull(
            message = "{version.protocol_version.required}",
            groups = ValidationSequence.NotBlankGroup.class
    )
    private Integer protocolVersion;
    
    @NotNull(
            message = "{version.pack_version.required}",
            groups = ValidationSequence.NotBlankGroup.class
    )
    private Integer packVersion;
    
    @NotNull(
            message = "{version.build_time.required}",
            groups = ValidationSequence.NotBlankGroup.class
    )
    private Instant buildTime;
    
    @NotNull(
            message = "{version.stable.required}",
            groups = ValidationSequence.NotBlankGroup.class
    )
    private Boolean stable;
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getReleaseTarget() {
        return releaseTarget;
    }
    
    public Integer getWorldVersion() {
        return worldVersion;
    }
    
    public Integer getProtocolVersion() {
        return protocolVersion;
    }
    
    public Integer getPackVersion() {
        return packVersion;
    }
    
    public Instant getBuildTime() {
        return buildTime;
    }
    
    public Boolean getStable() {
        return stable;
    }
}