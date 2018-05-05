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
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomixPropertySourceLocatorTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixPropertySourceLocatorTests.class);
    private static final String ROOT = UUID.randomUUID().toString();
    private static final String APPL_CONTEXT = "application";
    private static final String TEST_CONTEXT = "test-application";

    private AtomixService atomixService;
    private AtomixClient client;
    private AtomixConfigProperties properties;
    private ConfigurableApplicationContext context;
    private ConfigurableEnvironment environment;

    // *****************
    // Test setup
    // *****************

    @Before
    public void setUp() {
        this.atomixService = new AtomixService();
        this.atomixService.start();
        this.atomixService.getDocumentTree(ROOT).createRecursive(DocumentPath.from("root", APPL_CONTEXT, "props.p1"), "v1");
        this.atomixService.getDocumentTree(ROOT).createRecursive(DocumentPath.from("root", APPL_CONTEXT, "props.p2"), "v2");
        this.atomixService.getDocumentTree(ROOT).createRecursive(DocumentPath.from("root", TEST_CONTEXT, "props.p2"), "v2.1");

        this.context = new SpringApplicationBuilder(Config.class).web(WebApplicationType.NONE).run(
            "--banner.mode=OFF",
            "--spring.cloud.atomix.members[0].address=" + atomixService.getLocalMember().address().toString(),
            "--spring.cloud.atomix.members[0].id=" +  atomixService.getLocalMember().id().id(),
            "--spring.cloud.atomix.members[0].type=" + atomixService.getLocalMember().type().name(),
            "--spring.cloud.atomix.config.root=" + ROOT,
            "--spring.application.name=" + TEST_CONTEXT
        );

        this.client = this.context.getBean(AtomixClient.class);
        this.properties = this.context.getBean(AtomixConfigProperties.class);
        this.environment = this.context.getEnvironment();
    }

    @After
    public void tearDown() {
        if (this.atomixService != null) {
            this.atomixService.stop();
        }
    }

    // *****************
    // Tests
    // *****************

    @Test
    public void checkKeyValues() {
        assertThat(this.environment.getProperty("props.p1")).isEqualTo("v1");
        assertThat(this.environment.getProperty("props.p2")).isEqualTo("v2.1");
    }

    @Ignore
    @Test
    public void propertyLoadedAndUpdated() throws Exception {
        assertThat(this.environment.getProperty("props.p1")).isEqualTo("v1");

        this.atomixService.getDocumentTree(ROOT).set(DocumentPath.from("root", APPL_CONTEXT, "props.p1"), "v1.1");

        CountDownLatch latch = this.context.getBean(CountDownLatch.class);
        boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);
        assertThat(receivedEvent).isTrue();

        assertThat(this.environment.getProperty("props.p1")).isEqualTo("v1.1");
    }

    // *****************
    // test Config
    // *****************

    @Configuration
    @EnableAutoConfiguration
    static class Config {
        private AtomicBoolean ready = new AtomicBoolean(false);

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
