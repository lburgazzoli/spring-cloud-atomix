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

import io.atomix.cluster.Member;
import io.atomix.core.Atomix;
import io.atomix.core.profile.Profile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luca Burgazzoli
 */
public class AtomixAutoConfigurationTests {
    private AtomixClient bootstrap;

    @Before
    public void setUp() {
        final int port = SocketUtils.findAvailableTcpPort();

        bootstrap = new AtomixClient(
            Atomix.builder()
                .withLocalMember(
                    Member.builder("_test-service")
                        .withAddress("localhost:" + port)
                        .withType(Member.Type.PERSISTENT)
                        .build())
                .withMembers(
                    Member.builder("_test-service")
                        .withType(Member.Type.PERSISTENT)
                        .withAddress("localhost:" + port)
                        .build())
                .withProfiles(
                    Profile.DATA_GRID
                )
                .build()
        );

        bootstrap.start();
    }

    @After
    public void tearDown() {
        if (bootstrap != null) {
            bootstrap.stop();
        }
    }

	@Test
	public void testAtomixClientHasBeenInjected() {
        new ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    AtomixAutoConfiguration.class
                )
            )
            .withPropertyValues(
                "banner.mode=OFF",
                "spring.cloud.atomix.local-member.address=" + "localhost:" + SocketUtils.findAvailableTcpPort(),
                "spring.cloud.atomix.members[0].address=" + "localhost:" + bootstrap.getLocalMember().address().port(),
                "spring.cloud.atomix.members[0].id=" + bootstrap.getLocalMember().id().id(),
                "spring.cloud.atomix.members[0].type=" + bootstrap.getLocalMember().type().name()
            )
            .run((context) -> {
                    assertThat(context).getBeans(AtomixClient.class).hasSize(1);
                    assertThat(context).getBeans(AtomixClient.class).containsKeys("atomix-client");
                }
            );
	}
}
