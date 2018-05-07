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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import io.atomix.cluster.Member;
import org.springframework.cloud.atomix.AtomixConstants;
import org.springframework.cloud.client.ServiceInstance;

public class AtomixServiceInstance implements ServiceInstance {
    private final Member member;
    private final Map<String, String> meta;

    public AtomixServiceInstance(Member member) {
        this.member = member;

        Map<String, String> m = new HashMap<>(member.metadata());
        m.putIfAbsent(AtomixConstants.META_SERVICE_HOST, this.member.address().host());
        m.putIfAbsent(AtomixConstants.META_SERVICE_PORT, Integer.toString(this.member.address().port()));
        m.putIfAbsent(AtomixConstants.META_SERVICE_SCHEME,"http");
        m.putIfAbsent(AtomixConstants.META_SERVICE_RACK, member.rack());
        m.putIfAbsent(AtomixConstants.META_SERVICE_ZONE, member.zone());

        this.meta = ImmutableMap.copyOf(m);
    }

    @Override
    public String getServiceId() {
        return meta.get(AtomixConstants.META_SERVICE_ID);
    }

    @Override
    public String getHost() {
        return meta.get(AtomixConstants.META_SERVICE_HOST);
    }

    @Override
    public int getPort() {
        return Integer.parseInt(meta.get(AtomixConstants.META_SERVICE_PORT));
    }

    @Override
    public boolean isSecure() {
        return Objects.equals(
            "https",
            meta.get(AtomixConstants.META_SERVICE_SCHEME)
        );
    }

    @Override
    public URI getUri() {
        String scheme = meta.get(AtomixConstants.META_SERVICE_SCHEME);
        if (scheme == null && isSecure()) {
            scheme = "https";
        }

        return URI.create(
            String.format("%s://%s:%d", scheme, getHost(), getPort())
        );
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.meta;
    }
}
