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

import io.atomix.cluster.Member;
import io.atomix.core.Atomix;
import io.atomix.core.profile.Profile;
import org.springframework.context.Lifecycle;
import org.springframework.util.SocketUtils;

public class AtomixService implements Lifecycle {
    private final Atomix atomix;

    public AtomixService() {
        // dynamically find a free port
        final int port = SocketUtils.findAvailableTcpPort();

        // build the atomix service
        this.atomix = Atomix.builder()
            .withLocalMember(
                Member.builder("test")
                    .withAddress("localhost:" + port)
                    .withType(Member.Type.EPHEMERAL)
                    .build())
            .withMembers(
                Member.builder("test")
                    .withType(Member.Type.EPHEMERAL)
                    .withAddress("localhost:" + port)
                    .build())
            .withProfiles(
                Profile.DATA_GRID
            )
            .build();
    }

    public int port() {
        return this.atomix.membershipService().getLocalMember().address().port();
    }

    public Atomix atomix() {
        return this.atomix;
    }

    @Override
    public void start() {
        if (!this.atomix.isRunning()) {
            this.atomix.start().join();
        }
    }

    @Override
    public void stop() {
        this.atomix.stop().join();
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

    public Collection<Member> getMembers() {
        return this.atomix.membershipService().getMembers();
    }
}
