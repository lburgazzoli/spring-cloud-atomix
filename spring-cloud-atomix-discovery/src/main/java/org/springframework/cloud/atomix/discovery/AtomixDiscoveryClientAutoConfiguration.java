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

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * @author Luca Burgazzoli
 * @since 1.1.0
 */
@Configuration
@ConditionalOnZookeeperDiscoveryEnabled
@AutoConfigureBefore({CommonsClientAutoConfiguration.class, SimpleDiscoveryClientAutoConfiguration.class})
@EnableConfigurationProperties
public class AtomixDiscoveryClientAutoConfiguration {
}
