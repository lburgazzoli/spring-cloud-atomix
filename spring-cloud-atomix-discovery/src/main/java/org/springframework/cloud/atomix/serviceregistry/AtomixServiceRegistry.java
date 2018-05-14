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

package org.springframework.cloud.atomix.serviceregistry;

import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * @author Luca Burgazzoli
 */
public class AtomixServiceRegistry implements ServiceRegistry<Registration> {
    private final AtomixClient client;
    private final AtomixServiceRegistryConfiguration configuration;

    public AtomixServiceRegistry(AtomixClient client, AtomixServiceRegistryConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    public void register(Registration registration) {
        //AtomixConstants.META_SERVICE_ID;
    }

    @Override
    public void deregister(Registration registration) {
    }

    @Override
    public void setStatus(Registration registration, String status) {

    }

    @Override
    public <T> T getStatus(Registration registration) {
        return null;
    }

    @Override
    public void close() {
    }
}
