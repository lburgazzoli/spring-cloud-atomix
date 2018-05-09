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

import java.util.Map;
import java.util.stream.Stream;

import io.atomix.cluster.Member;
import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.cloud.atomix.AtomixConstants;

public final class AtomixDiscoveryUtils {
    private AtomixDiscoveryUtils() {
    }

    public static Stream<Member> getServices(AtomixClient client, AtomixDiscoveryConfiguration properties) {
        return client.getMembers().stream()
            .filter(member -> {
                return member.metadata().containsKey(AtomixConstants.META_SERVICE_ID);
            })
            .filter(member -> {
                final Map<String, String> metadata = member.metadata();
                final String serviceId = metadata.get(AtomixConstants.META_SERVICE_ID);
                final AtomixDiscoveryConfiguration.ServiceConfig serviceConfig = properties.getServices().get(serviceId);

                if (serviceConfig != null) {
                    return metadata.isEmpty() || metadata.entrySet().containsAll(serviceConfig.getMetadata().entrySet());
                }

                return true;
            });
    }
}
