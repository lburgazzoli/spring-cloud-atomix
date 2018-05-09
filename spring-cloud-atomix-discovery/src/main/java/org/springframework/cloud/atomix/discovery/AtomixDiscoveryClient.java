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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.cloud.atomix.AtomixConstants;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * @author Luca Burgazzoli
 */
public class AtomixDiscoveryClient implements DiscoveryClient {
    private final AtomixClient client;
    private final AtomixDiscoveryConfiguration properties;

    public AtomixDiscoveryClient(AtomixClient client, AtomixDiscoveryConfiguration properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public String description() {
        return "Spring Cloud Atomix Discovery Client";
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        if (serviceId == null) {
            return Collections.emptyList();
        }

        return AtomixDiscoveryUtils.getServices(this.client, this.properties)
            .filter(member -> Objects.equals(serviceId, member.metadata().get(AtomixConstants.META_SERVICE_ID)))
            .map(AtomixServiceInstance::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getServices() {
        return AtomixDiscoveryUtils.getServices(this.client, this.properties)
            .map(member -> member.metadata().get(AtomixConstants.META_SERVICE_ID))
            .distinct()
            .collect(Collectors.toList());
    }
}
