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

import io.atomix.cluster.Member.Type;
import io.atomix.cluster.MemberConfig;
import io.atomix.cluster.MemberId;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luca Burgazzoli
 */
public class AtomixAutoConfigurationTests {

    @Ignore
	@Test
	public void testAtomixClientHasBeenInjected() {
        new ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    AtomixAutoConfigurationTests.TestConfig.class,
                    AtomixAutoConfiguration.class
                )
            )
            .run((context) -> {
                    assertThat(context).hasSingleBean(AtomixService.class);
                }
            );
	}

	static class TestConfig {
		@Bean
		AtomixProperties atomixProperties(AtomixService testingService) {
            // Local client does not need to have host/port
            MemberConfig local = new MemberConfig();
            local.setAddress("localhost:" + SocketUtils.findAvailableTcpPort());
            local.setType(Type.EPHEMERAL);
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

        @Bean(initMethod = "start", destroyMethod = "stop") 
        AtomixService testingService() throws Exception {
			return new AtomixService();
		}
	}
}
