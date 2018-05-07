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

import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.atomix.AtomixConstants;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomixDiscoveryClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixDiscoveryClientTest.class);
    private static final String TEST_CONTEXT = "test-application";

    private AtomixService bootstrap;

    // *****************
    // Test setup
    // *****************

    @Before
    public void setUp() {
        // Set up the boostrap node
        this.bootstrap = new AtomixService();
        this.bootstrap.start();

        this.bootstrap.client("s1i1", new HashMap<String, String>() {{
            put(AtomixConstants.META_SERVICE_ID, "my-service-1");
            put(AtomixConstants.META_SERVICE_ZONE, "US");
            put(AtomixConstants.META_SERVICE_RACK, "r1");
        }});
        this.bootstrap.client("s1i2", new HashMap<String, String>() {{
            put(AtomixConstants.META_SERVICE_ID, "my-service-1");
            put(AtomixConstants.META_SERVICE_ZONE, "US");
            put(AtomixConstants.META_SERVICE_RACK, "r2");
        }});
        this.bootstrap.client("s1i3", new HashMap<String, String>() {{
            put(AtomixConstants.META_SERVICE_ID, "my-service-1");
            put(AtomixConstants.META_SERVICE_ZONE, "EU");
            put(AtomixConstants.META_SERVICE_RACK, "r1");
        }});
        this.bootstrap.client("s1i4", new HashMap<String, String>() {{
            put(AtomixConstants.META_SERVICE_ID, "my-service-1");
            put(AtomixConstants.META_SERVICE_ZONE, "EU");
            put(AtomixConstants.META_SERVICE_RACK, "r2");
        }});
        this.bootstrap.client("s2i1", new HashMap<String, String>() {{
            put(AtomixConstants.META_SERVICE_ID, "my-service-2");
            put(AtomixConstants.META_SERVICE_ZONE, "EU");
            put(AtomixConstants.META_SERVICE_RACK, "r1");
        }});
    }

    @After
    public void tearDown() {
        if (this.bootstrap != null) {
            this.bootstrap.stop();
        }
    }

    // *****************
    // Tests
    // *****************

    @Test
    public void checkSimpleDiscovery() {
        ConfigurableApplicationContext context = null;

        try {
            context = new SpringApplicationBuilder(TestConfig.class).web(WebApplicationType.NONE).run(
                "--banner.mode=OFF",
                "--spring.cloud.atomix.local-member.address=" + "localhost:" + SocketUtils.findAvailableTcpPort(),
                "--spring.cloud.atomix.members[0].address=" + "localhost:" + bootstrap.getLocalMember().address().port(),
                "--spring.cloud.atomix.members[0].id=" + bootstrap.getLocalMember().id().id(),
                "--spring.cloud.atomix.members[0].type=" + bootstrap.getLocalMember().type().name(),
                "--spring.cloud.atomix.config.enabled=false",
                "--spring.cloud.atomix.discovery.enabled=true",
                "--ribbon.atomix.enabled=false",
                "--spring.application.name=" + TEST_CONTEXT
            );

            AtomixDiscoveryClient client = context.getBean(AtomixDiscoveryClient.class);
            List<ServiceInstance> s1instances = client.getInstances("my-service-1");
            List<ServiceInstance> s2instances = client.getInstances("my-service-2");

            assertThat(s1instances).hasSize(4);
            assertThat(s2instances).hasSize(1);
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    @Test
    public void checkDiscoveryWithFilters() {
        ConfigurableApplicationContext context = null;

        try {
            context = new SpringApplicationBuilder(TestConfig.class).web(WebApplicationType.NONE).run(
                "--banner.mode=OFF",
                "--spring.cloud.atomix.local-member.address=" + "localhost:" + SocketUtils.findAvailableTcpPort(),
                "--spring.cloud.atomix.members[0].address=" + "localhost:" + bootstrap.getLocalMember().address().port(),
                "--spring.cloud.atomix.members[0].id=" + bootstrap.getLocalMember().id().id(),
                "--spring.cloud.atomix.members[0].type=" + bootstrap.getLocalMember().type().name(),
                "--spring.cloud.atomix.config.enabled=false",
                "--spring.cloud.atomix.discovery.enabled=true",
                "--spring.cloud.atomix.discovery.services[my-service-1].metadata[service.zone]=EU",
                "--spring.cloud.atomix.discovery.services[my-service-2].metadata[service.zone]=US",
                "--ribbon.atomix.enabled=false",
                "--spring.application.name=" + TEST_CONTEXT
            );

            AtomixDiscoveryClient client = context.getBean(AtomixDiscoveryClient.class);
            List<ServiceInstance> s1instances = client.getInstances("my-service-1");
            List<ServiceInstance> s2instances = client.getInstances("my-service-2");

            assertThat(s1instances).hasSize(2);
            assertThat(s2instances).hasSize(0);
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    // *****************
    // test Config
    // *****************

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {
    }
}
