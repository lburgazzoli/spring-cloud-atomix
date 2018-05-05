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

import io.atomix.cluster.ClusterMembershipEvent;
import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.cloud.atomix.AtomixMemberWatch;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public class AtomixServiceWatch extends AtomixMemberWatch implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher publisher;

    public AtomixServiceWatch(AtomixClient client) {
        super(client);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void onMembershipChanged(ClusterMembershipEvent event) {
        publisher.publishEvent(
            new HeartbeatEvent(this, event.type())
        );
    }
}
