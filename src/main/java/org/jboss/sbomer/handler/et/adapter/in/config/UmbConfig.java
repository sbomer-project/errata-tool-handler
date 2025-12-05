package org.jboss.sbomer.handler.et.adapter.in.config;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.common.annotation.Identifier;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.core.net.PfxOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class UmbConfig {

    @ConfigProperty(name = "sbomer.umb.ssl", defaultValue = "true")
    boolean sslEnabled;

    @Produces
    @Identifier("umb")
    public AmqpClientOptions getClientOptions() {
        log.info("Setting up AMQP client options. SSL enabled: {}", sslEnabled);

        // FOR DEV PURPOSES If SSL is disabled, return simple options (No keystore checks)
        if (!sslEnabled) {
            log.warn("UMB SSL is DISABLED. This should only be used for local development.");
            return new AmqpClientOptions()
                    .setSsl(false)
                    .setConnectTimeout(30 * 1000)
                    .setReconnectInterval(5 * 1000);
        }


        String path = System.getenv("SBOMER_KEYSTORE_PATH");
        String password = System.getenv("SBOMER_KEYSTORE_PASSWORD");

        if (path == null || password == null) {
            throw new RuntimeException(
                    "The path or password to keystore was not provided. Set SBOMER_KEYSTORE_PATH and SBOMER_KEYSTORE_PASSWORD.");
        }
        if (Files.notExists(Path.of(path))) {
            throw new RuntimeException(
                    "The keystore file path provided by SBOMER_KEYSTORE_PATH does not exist: '" + path + "'");
        }

        log.debug("Using '{}' keystore to read certificates to connect to AMQP broker", path);

        return new AmqpClientOptions()
                .setSsl(true)
                .setConnectTimeout(30 * 1000)
                .setReconnectInterval(5 * 1000)
                .setPfxKeyCertOptions(new PfxOptions().setPath(path).setPassword(password));
    }
}