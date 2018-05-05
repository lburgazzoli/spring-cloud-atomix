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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.atomix.AtomixAutoConfiguration;
import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.cloud.atomix.ConditionalOnAtomixEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Bootstrap Configuration for Atomix Configuration
 *
 * @author Luca Burgazzoli
 */
@Configuration
@ConditionalOnAtomixEnabled
@Import(AtomixAutoConfiguration.class)
public class AtomixConfigBootstrapConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AtomixPropertySourceLocator atomixPropertySourceLocator(
            AtomixClient client,
            AtomixConfigProperties properties) {
        return new AtomixPropertySourceLocator(client, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AtomixConfigProperties atomixConfigProperties() {
        return new AtomixConfigProperties();
    }
}
