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
public class AtomixConfiguration {

    /**
     * Is Atomix enabled
     */
    private boolean enabled = true;

    /**
     * The atomix local member.
     */
    private LocalMemberConfig localMember = new LocalMemberConfig();

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

    public LocalMemberConfig getLocalMember() {
        return localMember;
    }

    public void setLocalMember(LocalMemberConfig localMember) {
        this.localMember = localMember;
    }

    public List<MemberConfig> getMembers() {
        return members;
    }

    public static class LocalMemberConfig {
        private String id;
        private Address address;

        public String getId() {
            return id;
        }

        public String getOrGenerateId() {
            return id != null ? id : UUID.randomUUID().toString();
        }

        public void setId(String id) {
            this.id = id;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }
}
