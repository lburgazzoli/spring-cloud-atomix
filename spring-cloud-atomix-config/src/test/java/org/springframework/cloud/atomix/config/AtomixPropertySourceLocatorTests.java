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

package org.springframework.cloud.atomix.config;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.atomix.cluster.Member;
import io.atomix.core.Atomix;
import io.atomix.core.profile.Profile;
import io.atomix.core.tree.DocumentPath;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
public class AtomixPropertySourceLocatorTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixPropertySourceLocatorTests.class);
    private static final String ROOT = UUID.randomUUID().toString();
    private static final String APPL_CONTEXT = "application";
    private static final String TEST_CONTEXT = "test-application";

    private AtomixService atomix;
    private ConfigurableApplicationContext context;

    // *****************
    // Test setup
    // *****************

    @Before
    public void setUp() {
        this.atomix = new AtomixService();
        this.atomix.start();
        this.atomix.getDocumentTree(ROOT).createRecursive(DocumentPath.from("root", APPL_CONTEXT, "props.p1"), "v1");
        this.atomix.getDocumentTree(ROOT).createRecursive(DocumentPath.from("root", APPL_CONTEXT, "props.p2"), "v2");
        this.atomix.getDocumentTree(ROOT).createRecursive(DocumentPath.from("root", TEST_CONTEXT, "props.p2"), "v2.1");

        this.context = new SpringApplicationBuilder(Config.class).web(WebApplicationType.NONE).run(
            "--banner.mode=OFF",
            "--spring.cloud.atomix.local-member.address=" + "localhost:" + SocketUtils.findAvailableTcpPort(),
            "--spring.cloud.atomix.members[0].address=" + "localhost:" + atomix.getPort(),
            "--spring.cloud.atomix.members[0].id=" +  atomix.getLocalMember().id().id(),
            "--spring.cloud.atomix.members[0].type=" + atomix.getLocalMember().type().name(),
            "--spring.cloud.atomix.config.root=" + ROOT,
            "--spring.application.name=" + TEST_CONTEXT
        );
    }

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
        if (this.atomix != null) {
            this.atomix.stop();
        }
    }

    // *****************
    // Tests
    // *****************

    @Test
    public void checkKeyValues() {
        assertThat(context.getEnvironment().getProperty("props.p1")).isEqualTo("v1");
        assertThat(context.getEnvironment().getProperty("props.p2")).isEqualTo("v2.1");
    }

    @Ignore // FIXME broken tests with boot 2.0.0
    @Test
    public void propertyLoadedAndUpdated() throws Exception {
        assertThat(context.getEnvironment().getProperty("props.p1")).isEqualTo("v1");

        this.atomix.getDocumentTree(ROOT).set(DocumentPath.from("root", APPL_CONTEXT, "props.p1"), "v1.1");

        CountDownLatch latch = context.getBean(CountDownLatch.class);
        latch.await();
        boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);
        assertThat(receivedEvent).isTrue();

        assertThat(context.getEnvironment().getProperty("props.p1")).isEqualTo("v1.1");
    }

    // *****************
    // test Config
    // *****************

    @Configuration
    @EnableAutoConfiguration
    static class Config {
        @Bean
        public CountDownLatch countDownLatch() {
            return new CountDownLatch(1);
        }

        @Bean
        public ContextRefresher contextRefresher(ConfigurableApplicationContext context, RefreshScope scope) {
            return new ContextRefresher(context, scope);
        }

        @EventListener
        public void handle(EnvironmentChangeEvent event) {
            LOGGER.debug("Event keys: " + event.getKeys());

            if (event.getKeys().contains("")) {
                countDownLatch().countDown();
            }
        }
    }
}
