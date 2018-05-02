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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import io.atomix.cluster.Member;
import io.atomix.core.Atomix;

public class AtomixClient implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixClient.class);

    private final Atomix atomix;

    public AtomixClient(Atomix atomix) {
        this.atomix = atomix;
    }

    // ************************
    // Lyfecycle
    // ************************

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }

        this.atomix.start()
            .thenApply(
                atomix -> {
                    LOGGER.debug("connected to atomix cluster");
                    return atomix;
                }
            )
            .join();
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }

        this.atomix.stop().thenRun(
            () -> {
                LOGGER.debug("disconnected from atomix cluster");
            }
        ).join();
    }

    @Override
    public boolean isRunning() {
        return this.atomix.isRunning();
    }

    // ************************
    // Access Atomix services
    // ************************
    
    public Member getLocalMember() {
        return this.atomix.membershipService().getLocalMember();
    }
} 