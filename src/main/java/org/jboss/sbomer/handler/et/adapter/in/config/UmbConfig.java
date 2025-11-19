package org.jboss.sbomer.handler.et.adapter.in.config;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Configuration for the UMB (Universal Message Bus) integration.
 * Maps properties starting with "sbomer.features.umb"
 */
@ConfigMapping(prefix = "sbomer.features.umb")
public interface UmbConfig {

    @WithName("enabled")
    Optional<Boolean> isEnabled();

    @WithName("trigger-topic")
    Optional<String> triggerTopic();

    @WithName("consumer-topic")
    Optional<String> consumerTopic();

    @WithName("producer-topic")
    Optional<String> producerTopic();

    @WithName("keystore")
    Optional<String> keystore();

    @WithName("keystore-password")
    Optional<String> keystorePassword();

    @WithName("truststore")
    Optional<String> truststore();

    @WithName("truststore-password")
    Optional<String> truststorePassword();
}