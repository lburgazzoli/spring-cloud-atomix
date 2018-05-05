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

import java.util.concurrent.atomic.AtomicBoolean;

import io.atomix.cluster.ClusterMembershipEvent;
import io.atomix.cluster.ClusterMembershipEventListener;
import io.atomix.cluster.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

public abstract class AtomixMemberWatch implements Lifecycle, ClusterMembershipEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixMemberWatch.class);

    private final AtomixClient client;
    private final AtomicBoolean running;

    public AtomixMemberWatch(AtomixClient client) {
        this.client = client;
        this.running = new AtomicBoolean(false);
    }
    
    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {
            client.getMemberhipService().addListener(this);
        }
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {
            client.getMemberhipService().removeListener(this);
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public void onEvent(ClusterMembershipEvent event) {
        final Member subject = event.subject();
        final Member local = client.getLocalMember();

        if (subject != null && local != null && !local.equals(subject)) {
            LOGGER.debug("Received member update from atomix: {}", event);

            onMembershipChanged(event);
        }
    }

    protected abstract void onMembershipChanged(ClusterMembershipEvent event);
}
