package org.jboss.sbomer.config;

import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.openfeature.contrib.providers.unleash.UnleashProvider;
import dev.openfeature.contrib.providers.unleash.UnleashProviderConfig;
import dev.openfeature.contrib.providers.unleash.UnleashProviderConfig.UnleashProviderConfigBuilder;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;
import io.getunleash.util.UnleashConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class FeatureFlagConfig {

    @ConfigProperty(name = "unleash.api.url", defaultValue = "http://localhost:4242/api")
    String unleashUrl;

    @ConfigProperty(name = "unleash.api.token", defaultValue = "default:development.unleash-insecure-api-token")
    String unleashToken;

    @ConfigProperty(name = "unleash.app.name", defaultValue = "unleash-onboarding-java")
    String unleashAppName;

    @ConfigProperty(name = "unleash.environment", defaultValue = "development")
    String environment;

    @ConfigProperty(name = "unleash.instance.id", defaultValue = "unleash-onboarding-instance")
    String instanceId;

    @PostConstruct
    void init() {
        log.info("----------------------------------------------------------------");
        log.info("UNLEASH INIT: Attempting to connect...");
        log.info("   > URL: {}", unleashUrl); 
        log.info("   > App Name: {}", unleashAppName);
        log.info("----------------------------------------------------------------");

        UnleashProviderConfigBuilder providerConfigBuilder = UnleashProviderConfig.builder()
                .unleashConfigBuilder(UnleashConfig.builder()
                        .appName(unleashAppName)
                        .instanceId(instanceId)
                        .unleashAPI(unleashUrl)
                        .apiKey(unleashToken)
                        .environment(environment)
                        .fetchTogglesInterval(1) 
                        .fetchTogglesConnectTimeout(Duration.ofSeconds(2))
                        .synchronousFetchOnInitialisation(true));

        UnleashProvider provider = new UnleashProvider(providerConfigBuilder.build());
        
        try {
            OpenFeatureAPI.getInstance().setProviderAndWait(provider);
            log.info("UNLEASH INIT: SUCCESS! Connected to Unleash.");
        } catch (Exception e) {
            log.error("UNLEASH INIT: FAILED to initialize Unleash provider.", e);
        }
    }

    @Produces
    public Client featureClient() {
        return OpenFeatureAPI.getInstance().getClient();
    }
}