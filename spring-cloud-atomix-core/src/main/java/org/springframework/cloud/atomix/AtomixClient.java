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

import java.util.Collection;

import io.atomix.cluster.ClusterMembershipService;
import io.atomix.cluster.Member;
import io.atomix.core.Atomix;
import io.atomix.core.tree.DocumentTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

public class AtomixClient implements Lifecycle {
    private final Logger logger;
    private final Atomix atomix;

    public AtomixClient(Atomix atomix) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.atomix = atomix;
    }

    // ************************
    // Lifecycle
    // ************************

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }

        logger.debug("starting atomix (local: {}, members: {})", atomix.membershipService().getLocalMember(), atomix.membershipService().getMembers());
        this.atomix.start()
            .thenRun(
                () -> {
                    logger.debug("started atomix (local: {}, members: {})", atomix.membershipService().getLocalMember(), atomix.membershipService().getMembers());
                }
            )
            .join();
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }

        logger.debug("stopping atomix (local: {}, members: {})", atomix.membershipService().getLocalMember(), atomix.membershipService().getMembers());
        this.atomix.stop().thenRun(
            () -> {
                logger.debug("stopped atomix (local: {}, members: {})", atomix.membershipService().getLocalMember(), atomix.membershipService().getMembers());
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
        return getMemberhipService().getLocalMember();
    }

    public Collection<Member> getMembers() {
        return getMemberhipService().getMembers();
    }

    public <T> DocumentTree<T> getDocumentTree(String name) {
        return this.atomix.getDocumentTree(name);
    }

    public ClusterMembershipService getMemberhipService() {
        return this.atomix.membershipService();
    }

    // ************************
    // Helpers
    // ************************
    
    protected Atomix atomix() {
        return this.atomix;
    }
} 