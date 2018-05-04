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

import java.util.List;
import java.util.stream.Collectors;

import io.atomix.cluster.Member;
import io.atomix.cluster.MemberConfig;
import io.atomix.cluster.MemberId;
import org.springframework.context.annotation.Bean;
import org.springframework.util.SocketUtils;

public class AtomixTestConfig {
    @Bean
    AtomixProperties atomixProperties(AtomixService testingService) {
        MemberConfig local = new MemberConfig();
        local.setAddress("localhost:" + SocketUtils.findAvailableTcpPort());
        local.setType(Member.Type.EPHEMERAL);
        local.setId(MemberId.from("client"));

        List<MemberConfig> members = testingService.getMembers().stream()
            .map(m -> {
                MemberConfig config = new MemberConfig();
                config.setId(m.id());
                config.setType(m.type());
                config.setAddress(m.address());

                return config;
            })
            .collect(Collectors.toList());

        AtomixProperties properties = new AtomixProperties();
        properties.setLocalMember(local);
        properties.setMembers(members);

        return properties;
    }

    @Bean(name = "testing-service", initMethod = "start", destroyMethod = "stop")
    AtomixService testingService() {
        return new AtomixService();
    }
}