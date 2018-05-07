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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.atomix.cluster.Member;
import io.atomix.core.Atomix;
import io.atomix.core.profile.Profile;
import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.util.SocketUtils;

public class AtomixService extends AtomixClient {
    private List<Atomix> clients;

    public AtomixService() {
        super(
            createAtomixInstance()
        );

        this.clients = new ArrayList<>();
    }


    @Override
    public void stop() {
        clients.stream().forEach(
            client -> client.stop().join()
        );

        super.stop();
    }

    public Atomix client(String id, Map<String, String> metadata) {
        Atomix client = Atomix.builder()
            .withLocalMember(
                Member.builder(id)
                    .withAddress("localhost", SocketUtils.findAvailableTcpPort())
                    .withType(Member.Type.EPHEMERAL)
                    .withMetadata(metadata)
                    .build()
            ).withMembers(
                getLocalMember()
            ).withProfiles(
                Profile.CLIENT
            ).build();

        client.start();

        return client;
    }

    // ************************
    // Helpers
    // ************************

    private static Atomix createAtomixInstance() {
        // dynamically find a free port
        final int port = SocketUtils.findAvailableTcpPort();

        // build the atomix service
        return Atomix.builder()
            .withLocalMember(
                Member.builder("_test-service")
                    .withAddress("localhost:" + port)
                    .withType(Member.Type.PERSISTENT)
                    .build())
            .withMembers(
                Member.builder("_test-service")
                    .withType(Member.Type.PERSISTENT)
                    .withAddress("localhost:" + port)
                    .build())
            .withProfiles(
                Profile.DATA_GRID
            )
            .build();
    }
}
