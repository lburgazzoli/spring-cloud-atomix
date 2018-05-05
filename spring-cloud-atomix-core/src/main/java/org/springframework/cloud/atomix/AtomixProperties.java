/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.atomix;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.atomix.cluster.Member;
import io.atomix.cluster.MemberConfig;
import io.atomix.utils.net.Address;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties related to connecting to Atomix
 *
 * @author Luca Burgazzoli
 * @since 2.0.0
 */
@Validated
@ConfigurationProperties("spring.cloud.atomix")
public class AtomixProperties {

    /**
     * Is Atomix enabled
     */
    private boolean enabled = true;

    /**
     * The atomix local member.
     */
    private MemberConfig localMember = defaultLocalMember();

    /**
     * The atomix nodes to connect to.
     */
    private List<MemberConfig> members = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public MemberConfig getLocalMember() {
        return localMember;
    }

    public void setLocalMember(MemberConfig localMember) {
        this.localMember = localMember;
    }

    public List<MemberConfig> getMembers() {
        return members;
    }

    /**
     * Helper to generate a default local member with a random id and listening
     * on a random port.
     */
    private static MemberConfig defaultLocalMember() {
        MemberConfig config = new MemberConfig();
        config.setType(Member.Type.EPHEMERAL);
        config.setAddress(Address.from(0));
        config.setId(UUID.randomUUID().toString());

        return config;
    }
}
