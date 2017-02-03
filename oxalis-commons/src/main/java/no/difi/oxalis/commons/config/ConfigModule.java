/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.commons.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.difi.oxalis.api.config.GlobalConfiguration;
import no.difi.oxalis.api.persistence.RepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Set;

/**
 * @author erlend
 * @since 4.0.0
 */
public class ConfigModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigModule.class);

    @Override
    protected void configure() {
        bind(GlobalConfiguration.class)
                .to(GlobalConfigurationTypesafeImpl.class)
                .in(javax.inject.Singleton.class);

        bind(RepositoryConfiguration.class)
                .to(RepositoryConfigurationImpl.class)
                .in(javax.inject.Singleton.class);

        Multibinder<PostConfig> postConfigMultibinder = Multibinder.newSetBinder(binder(), PostConfig.class);
        postConfigMultibinder.addBinding().to(JavaPropertiesPostConfig.class);
    }

    @Provides
    @Singleton
    @Named("file")
    protected Config loadConfigurationFile(@Named("home") Path homePath) {
        Path configPath = homePath.resolve("oxalis.conf");
        LOGGER.info("Configuration file: {}", configPath);

        return ConfigFactory.parseFile(configPath.toFile());
    }

    @Provides
    @Singleton
    protected Config loadConfiguration(@Named("file") Config config, Set<PostConfig> postConfigs) {
        Config referenceConfig = ConfigFactory.defaultReference();

        Config result = ConfigFactory.systemProperties()
                .withFallback(config)
                .withFallback(referenceConfig)
                .withFallback(referenceConfig.getConfig("defaults"));

        // Performs actions when configuration is loaded.
        postConfigs.forEach(pc -> pc.perform(result));

        return result;
    }
}