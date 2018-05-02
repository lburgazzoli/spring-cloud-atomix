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
import io.atomix.core.Atomix;
import io.atomix.core.profile.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

/**
 * @author Luca Burgazzoli
 */
public class SimpleTest {

    @Ignore
	@Test
	public void test() throws Exception {
	    final Logger logger = LoggerFactory.getLogger(getClass());
        final int clientPort = SocketUtils.findAvailableTcpPort();
        final int clusterPort = SocketUtils.findAvailableTcpPort();
        final CountDownLatch latch = new CountDownLatch(2);

        logger.info("******* Start cluster");
        Atomix cluster = createAtomixCluster(clusterPort);
        cluster.start()
            .thenApply(
                a -> {
                    logger.info("******* cluster started");
                    latch.countDown();
                    return a;
                }
            )
            .join();

        logger.info("####### Start client");        
        Atomix client = createAtomixClient(clientPort, clusterPort);
        client.start().thenApply(
            a -> {
                logger.info("####### client started");
                latch.countDown();
                return a;
            }
        ).join();

        latch.await();

        logger.info("####### Stop client");
        client.stop().join();

        logger.info("####### Stop cluster");
        cluster.stop().join();

	}

	private static Atomix createAtomixCluster(int clusterPort) {
        // build the atomix service
        return Atomix.builder()
            .withLocalMember(
                Member.builder("_test-cluster")
                    .withAddress("localhost:" + clusterPort)
                    .withType(Member.Type.PERSISTENT)
                    .build())
            .withMembers(
                Member.builder("_test-cluster")
                    .withType(Member.Type.PERSISTENT)
                    .withAddress("localhost:" + clusterPort)
                    .build())
            .withProfiles(
                Profile.DATA_GRID
            )
            .build();
    }

    private static Atomix createAtomixClient(int clientPort, int clusterPort) {
        // build the atomix service
        return Atomix.builder()
            .withLocalMember(
                Member.builder("_test-client")
                    .withAddress("localhost:" + clientPort)
                    .withType(Member.Type.EPHEMERAL)
                    .build())
            .withMembers(
                Member.builder("_test-cluster")
                    .withType(Member.Type.PERSISTENT)
                    .withAddress("localhost:" + clusterPort)
                    .build())
            .withProfiles(
                Profile.CLIENT
            )
            .build();
    }
}
