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

package org.springframework.cloud.atomix.discovery;

import java.util.List;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.atomix.AtomixConstants;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomixDiscoveryClientTest {
    private static final String TEST_CONTEXT = "test-application";

    @Rule
    public final AtomixService service = new AtomixService(c -> {
        c.client("s1i1", ImmutableMap.of(
            AtomixConstants.META_SERVICE_ID, "my-service-1",
            AtomixConstants.META_SERVICE_ZONE, "US",
            AtomixConstants.META_SERVICE_RACK, "r1"
        ));
        c.client("s1i2", ImmutableMap.of(
            AtomixConstants.META_SERVICE_ID, "my-service-1",
            AtomixConstants.META_SERVICE_ZONE, "US",
            AtomixConstants.META_SERVICE_RACK, "r2"
        ));
        c.client("s1i3", ImmutableMap.of(
            AtomixConstants.META_SERVICE_ID, "my-service-1",
            AtomixConstants.META_SERVICE_ZONE, "EU",
            AtomixConstants.META_SERVICE_RACK, "r1"
        ));
        c.client("s1i4", ImmutableMap.of(
            AtomixConstants.META_SERVICE_ID, "my-service-1",
            AtomixConstants.META_SERVICE_ZONE, "EU",
            AtomixConstants.META_SERVICE_RACK, "r2"
        ));
        c.client("s2i1", ImmutableMap.of(
            AtomixConstants.META_SERVICE_ID, "my-service-2",
            AtomixConstants.META_SERVICE_ZONE, "EU",
            AtomixConstants.META_SERVICE_RACK, "r1"
        ));
    });

    // *****************
    // Tests
    // *****************

    @Test
    public void checkSimpleDiscovery() {
        new ApplicationContextRunner()
            .withUserConfiguration(
                TestConfig.class
            )
            .withPropertyValues(
                "banner.mode=OFF",
                "spring.cloud.atomix.local-member.address=" + "localhost:" + SocketUtils.findAvailableTcpPort(),
                "spring.cloud.atomix.members[0].address=" + "localhost:" + service.atomix().getLocalMember().address().port(),
                "spring.cloud.atomix.members[0].id=" + service.atomix().getLocalMember().id().id(),
                "spring.cloud.atomix.members[0].type=" + service.atomix().getLocalMember().type().name(),
                "spring.cloud.atomix.config.enabled=false",
                "spring.cloud.atomix.discovery.enabled=true",
                "ribbon.atomix.enabled=false",
                "spring.application.name=" + TEST_CONTEXT
            )
            .run(
                context -> {
                    AtomixDiscoveryClient client = context.getBean(AtomixDiscoveryClient.class);
                    List<ServiceInstance> s1instances = client.getInstances("my-service-1");
                    List<ServiceInstance> s2instances = client.getInstances("my-service-2");

                    assertThat(s1instances).hasSize(4);
                    assertThat(s2instances).hasSize(1);
                }
            );
    }

    @Test
    public void checkDiscoveryWithFilters() {
        new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                "banner.mode=OFF",
                "spring.cloud.atomix.local-member.address=" + "localhost:" + SocketUtils.findAvailableTcpPort(),
                "spring.cloud.atomix.members[0].address=" + "localhost:" + service.atomix().getLocalMember().address().port(),
                "spring.cloud.atomix.members[0].id=" + service.atomix().getLocalMember().id().id(),
                "spring.cloud.atomix.members[0].type=" + service.atomix().getLocalMember().type().name(),
                "spring.cloud.atomix.config.enabled=false",
                "spring.cloud.atomix.discovery.enabled=true",
                "spring.cloud.atomix.discovery.services[my-service-1].metadata[service.zone]=EU",
                "spring.cloud.atomix.discovery.services[my-service-2].metadata[service.zone]=US",
                "ribbon.atomix.enabled=false",
                "spring.application.name=" + TEST_CONTEXT
            )
            .run(
                context -> {
                    AtomixDiscoveryClient client = context.getBean(AtomixDiscoveryClient.class);
                    List<ServiceInstance> s1instances = client.getInstances("my-service-1");
                    List<ServiceInstance> s2instances = client.getInstances("my-service-2");

                    assertThat(s1instances).hasSize(2);
                    assertThat(s2instances).hasSize(0);
                }
            );
    }

    // *****************
    // test Config
    // *****************

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {
    }
}
