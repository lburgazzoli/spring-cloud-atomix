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

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luca Burgazzoli
 */
public class AtomixAutoConfigurationTests {
	@Test
	public void testAtomixClientHasBeenInjected() {
        new ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    AtomixTestConfig.class,
                    AtomixAutoConfiguration.class
                )
            )
            .withSystemProperties(
                "logging.level.root=INFO"
            )
            .withPropertyValues(
                "banner.mode=OFF"
            )
            .run((context) -> {
                    assertThat(context).getBeans(AtomixClient.class).hasSize(2);
                    assertThat(context).getBeans(AtomixClient.class).containsKeys("testing-service", "atomix-client");
                    assertThat(context).hasSingleBean(AtomixService.class);
                }
            );
	}
}
