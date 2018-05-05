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

import java.util.UUID;
import javax.lang.model.element.TypeParameterElement;

import io.atomix.cluster.Member;
import io.atomix.utils.net.Address;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.atomix.core.Atomix;
import io.atomix.core.AtomixConfig;
import io.atomix.core.profile.Profile;
import org.springframework.util.SocketUtils;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration}
 * that sets up Atomix discovery.
 *
 * @author Luca Burgazzoli
 * @since 2.0.0
 */
@Configuration
@ConditionalOnAtomixEnabled
@EnableConfigurationProperties
public class AtomixAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(AtomixProperties.class)
    public AtomixProperties atomixProperties() {
        return new AtomixProperties();
    }

    @Bean(name = "atomix-client", initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public AtomixClient atomixClient(AtomixProperties properties) {
        final AtomixConfig config = new AtomixConfig();

        // This is an ephemeral/client instance, not a data node
        config.addProfile(Profile.CLIENT);

        // set the local member
        config.getClusterConfig().setLocalMember(properties.getLocalMember());

        // add members of the cluster
        properties.getMembers().forEach(member -> config.getClusterConfig().addMember(member));

        return new AtomixClient(Atomix.builder(config).build());
    }

    @Configuration
    @ConditionalOnClass(Endpoint.class)
    protected static class AtomixHealthConfig {
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(Atomix.class)
        @ConditionalOnEnabledHealthIndicator("atomix")
        public AtomixHealthIndicator atomixHealthIndicator(AtomixClient atomix) {
            return new AtomixHealthIndicator(atomix);
        }
    }
}
