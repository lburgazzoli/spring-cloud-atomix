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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.atomix.core.tree.DocumentPath;
import io.atomix.core.tree.DocumentTree;
import io.atomix.core.tree.NoSuchDocumentPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.atomix.AtomixClient;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ReflectionUtils;

/**
 * Atomix provides a hierarchical <a href="https://en.wikipedia.org/wiki/Document_Object_Model">document tree</a> data
 * structure to store arbitrary data, such as configuration data. Spring Cloud Atomix Config is an alternative to the
 * <a href="https://github.com/spring-cloud/spring-cloud-config">Config Server and Client</a>.  Configuration is loaded
 * into the Spring Environment during the special "bootstrap" phase.  Configuration is stored in the {@code config}
 * document by default. Multiple {@code PropertySource} instances are created based on the application's name and the
 * active profiles that mimics the Spring Cloud Config order of resolving properties. For example, an application with
 * the name "testApp" and with the "dev" profile will have the following property sources created:
 *
 * <pre>{@code
 * testApp:dev
 * testApp
 * application:dev
 * application
 * }</pre>
 *
 * </p>
 * The most specific property source is at the top, with the least specific at the bottom. Properties is the {@code config:application}
 * namespace are applicable to all applications using atomix for configuration. Properties in the {@code config/testApp}
 * namespace are only available to the instances of the service named "testApp".
 *
 * @author Luca Burgazzoli
 */
public class AtomixPropertySourceLocator implements PropertySourceLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixPropertySourceLocator.class);

    private final AtomixClient client;
    private final AtomixConfigProperties properties;
    private final List<String> contexts;

    public AtomixPropertySourceLocator(AtomixClient client, AtomixConfigProperties properties) {
        this.client = client;
        this.properties = properties;
        this.contexts = new ArrayList<>();
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            final ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
            final String appName = env.getProperty(AtomixConfigConstants.PROPERTY_SPRING_APPLICATION_NAME, this.properties.getDefaultContext());

            // Set-up defaults
            setupContext(contexts, env.getActiveProfiles(), this.properties.getDefaultContext());

            // No need to set-up context as no specific application name is set
            if (!Objects.equals(appName, this.properties.getDefaultContext())) {
                setupContext(contexts, env.getActiveProfiles(), appName);
            }

            CompositePropertySource composite = new CompositePropertySource(AtomixConfigConstants.NAME);
            Collections.reverse(contexts);

            LOGGER.debug("Context load order: {}", contexts);

            for (String context : contexts) {
                LOGGER.debug("Load properties for context: {}", context);

                try {
                    composite.addPropertySource(
                        new MapPropertySource(context, loadProperties(context))
                    );
                } catch (Exception e) {
                    if (this.properties.isFailFast()) {
                        ReflectionUtils.rethrowRuntimeException(e);
                    } else {
                        LOGGER.warn("Unable to load atomix config from {} ", context, e);
                    }
                }
            }

            return composite;
        }

        return null;
    }

    private void setupContext(List<String> contexts, String[] profiles, String item) {
        contexts.add(item);

        for (String profile : profiles) {
            contexts.add(item + AtomixConfigConstants.PROFILE_SEPARATOR + profile);
        }
    }

    private Map<String, Object> loadProperties(String context) {
        final DocumentTree<String> tree = client.getDocumentTree(this.properties.getRoot());
        final Map<String, Object> properties = new HashMap<>();

        traverse(tree, context, properties);

        return Collections.unmodifiableMap(properties);
    }

    private Map<String, Object> traverse(DocumentTree<String> tree, String path, Map<String, Object> properties) {
        try {
            String fullPath = "root" + AtomixConfigConstants.PATH_SEPARATOR + path;

            tree.getChildren(DocumentPath.from(fullPath)).forEach(
                (k, v) -> {
                    properties.put(k, v.value());

                    traverse(tree, path + AtomixConfigConstants.PATH_SEPARATOR + k, properties);
                }
            );
        } catch(NoSuchDocumentPathException e) {
            // ignore
        }

        return properties;
    }

    List<String> getContexts() {
        return contexts;
    }
}
