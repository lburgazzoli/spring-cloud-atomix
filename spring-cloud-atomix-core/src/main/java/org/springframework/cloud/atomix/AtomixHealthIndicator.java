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

import io.atomix.cluster.Member;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * A {@link org.springframework.boot.actuate.health.HealthIndicator} that checks the
 * status of the Atomix connection.
 *
 * @author Luca Burgazzoli
 * @since 2.0.0
 */
public class AtomixHealthIndicator extends AbstractHealthIndicator {
    private final AtomixClient atomix;

    public AtomixHealthIndicator(AtomixClient atomix) {
        this.atomix = atomix;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (this.atomix.isRunning()) {
            builder.down().withDetail("error", "Atomix client not running");
        } else {
            builder.up();
        }

        final Member member = this.atomix.getLocalMember();

        builder.withDetail("memberId", member.id().id());
        builder.withDetail("rack", member.rack());
        builder.withDetail("zone", member.zone());

        try {
            builder.withDetail("state", member.getState().name());
        } catch (UnsupportedOperationException e) {
            // ignore
        }

        member.metadata().forEach(builder::withDetail);
    }
}
