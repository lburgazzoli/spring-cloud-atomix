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

package org.springframework.cloud.atomix.config;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.atomix.core.tree.DocumentTree;
import io.atomix.core.tree.DocumentTreeEvent;
import io.atomix.core.tree.DocumentTreeListener;
import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.Lifecycle;

/**
 * Class that registers a {@link DocumentTreeListener} for each context.
 * It publishes events upon element change in Atomix.
 *
 * @author Luca Burgazzoli
 */
public class AtomixConfigWatcher implements Lifecycle, DocumentTreeListener<String>, ApplicationEventPublisherAware {
    private final AtomixClient client;
    private final AtomixConfigConfiguration configProperties;
    private final List<String> contexts;
    private final AtomicBoolean running;

    private DocumentTree<String> tree;
    private ApplicationEventPublisher publisher;

    public AtomixConfigWatcher(AtomixClient client, AtomixConfigConfiguration configProperties, List<String> contexts) {
        this.client = client;
        this.configProperties = configProperties;
        this.contexts = contexts;
        this.running = new AtomicBoolean(false);
    }

    @Override
    public void event(DocumentTreeEvent<String> event) {
        if (isRunning() && Objects.nonNull(this.publisher)) {
            this.publisher.publishEvent(new RefreshEvent(this, event, getEventDesc(event)));
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {
            this.tree = client.getDocumentTree(configProperties.getRoot());
            this.tree.addListener(this);

            //for (String context: contexts) {
            //    this.tree.addListener(DocumentPath.from("root" + AtomixConstants.PATH_SEPARATOR + context), this);
            //}
        }
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {
            if (this.tree != null) {
                this.tree.removeListener(this);
            }

            this.tree = null;
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    private String getEventDesc(DocumentTreeEvent<String> event) {
        StringBuilder out = new StringBuilder();
        out.append("type=").append(event.type());
        out.append(", path=").append(event.path().toString());

        event.newValue().ifPresent(
            v -> {
                out.append(", data=").append(v.value());
                out.append(", version=").append(v.version());
            }
        );

        return out.toString();
    }
}
