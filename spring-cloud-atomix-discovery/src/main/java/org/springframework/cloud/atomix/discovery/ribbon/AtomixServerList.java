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

package org.springframework.cloud.atomix.discovery.ribbon;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.cloud.atomix.AtomixConstants;
import org.springframework.cloud.atomix.discovery.AtomixDiscoveryProperties;
import org.springframework.cloud.atomix.discovery.AtomixDiscoveryUtils;

/**
 * @author Luca Burgazzoli
 */
public class AtomixServerList extends AbstractServerList<AtomixServer> {
    private final AtomixClient client;
    private final AtomixDiscoveryProperties properties;
    private String serviceId;

    public AtomixServerList(AtomixClient client, AtomixDiscoveryProperties properties, String serviceId) {
        this.client = Objects.requireNonNull(client);
        this.properties = Objects.requireNonNull(properties);
        this.serviceId = Objects.requireNonNull(serviceId);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig config) {
        this.serviceId = config.getClientName();
    }

    @Override
    public List<AtomixServer> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<AtomixServer> getUpdatedListOfServers() {
        return getServers();
    }

    private List<AtomixServer> getServers() {
        return AtomixDiscoveryUtils.getServices(client, properties)
            .filter(member -> {
                return Objects.equals(
                    member.metadata().get(AtomixConstants.META_SERVICE_ID), 
                    serviceId
                );
            })
            .map(member -> {
                return new AtomixServer(
                    serviceId, 
                    member.id().id(), 
                    member.address().host(), 
                    member.address().port());
            })
            .collect(Collectors.toList());
    }
}
