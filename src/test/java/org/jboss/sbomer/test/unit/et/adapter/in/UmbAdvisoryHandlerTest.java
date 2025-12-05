package org.jboss.sbomer.test.unit.et.adapter.in;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.sbomer.handler.et.adapter.in.UmbAdvisoryHandler;
import org.jboss.sbomer.handler.et.core.port.api.AdvisoryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.openfeature.sdk.Client;
import io.smallrye.reactive.messaging.amqp.IncomingAmqpMetadata;
import io.vertx.core.json.JsonObject;

@ExtendWith(MockitoExtension.class)
class UmbAdvisoryHandlerTest {

    @Mock
    AdvisoryHandler advisoryHandler;

    @Mock
    Client featureClient;


    @InjectMocks
    UmbAdvisoryHandler umbAdvisoryHandler;

    @BeforeEach
    void setup() {
        // Default behavior: Feature flag is ENABLED for all tests unless specified otherwise
        // matches: getBooleanValue("umb.handler.enabled", true)
        lenient().when(featureClient.getBooleanValue(eq("umb.handler.enabled"), anyBoolean()))
                .thenReturn(true);
    }

    @Test
    void shouldTriggerGenerationForQEStatus() {
        // Given a valid QE status payload
        String payload = new JsonObject()
                .put("errata_id", 12345)
                .put("errata_status", "QE")
                .put("fulladvisory", "RHBA-2023:12345")
                .encode();

        Message<byte[]> message = mockMessage(payload, "errata.activity.status");

        // When processed
        umbAdvisoryHandler.process(message);

        // Then the domain handler should be called with the ID
        verify(advisoryHandler).requestGenerations("12345");
        verify(message).ack();
    }

    @Test
    void shouldTriggerGenerationForShippedLiveStatus() {
        // Given a valid SHIPPED_LIVE status payload
        String payload = new JsonObject()
                .put("errata_id", 99999)
                .put("errata_status", "SHIPPED_LIVE")
                .encode();

        Message<byte[]> message = mockMessage(payload, "errata.activity.status");

        // When processed
        umbAdvisoryHandler.process(message);

        // Then the domain handler should be called
        verify(advisoryHandler).requestGenerations("99999");
        verify(message).ack();
    }

    @Test
    void shouldIgnoreIrrelevantStatus() {
        // Given a status that is NOT a trigger (e.g., NEW_FILES)
        String payload = new JsonObject()
                .put("errata_id", 12345L)
                .put("errata_status", "NEW_FILES")
                .encode();

        Message<byte[]> message = mockMessage(payload, "errata.activity.status");

        // When processed
        umbAdvisoryHandler.process(message);

        // Then the domain handler should NOT be called, but message should be acked
        verify(advisoryHandler, never()).requestGenerations(anyString());
        verify(message).ack();
    }

    @Test
    void shouldIgnoreInvalidSubject() {
        // Given a valid payload but wrong subject
        String payload = new JsonObject()
                .put("errata_id", 12345)
                .put("errata_status", "QE")
                .encode();

        Message<byte[]> message = mockMessage(payload, "some.other.topic");

        // When processed
        umbAdvisoryHandler.process(message);

        // Then ignored
        verify(advisoryHandler, never()).requestGenerations(anyString());
        verify(message).ack();
    }

    @Test
    void shouldHandleMalformedJsonGracefully() {
        // Given invalid JSON
        String payload = "{ NOT VALID JSON }";
        Message<byte[]> message = mockMessage(payload, "errata.activity.status");

        // When processed
        umbAdvisoryHandler.process(message);

        // Then it should assume it's garbage and Ack it to remove from queue
        verify(advisoryHandler, never()).requestGenerations(anyString());
        verify(message).ack();
    }

    @Test
    void shouldSkipProcessingWhenFlagDisabled() {
        // Given the feature flag is DISABLED
        when(featureClient.getBooleanValue(eq("umb.handler.enabled"), anyBoolean()))
                .thenReturn(false);

        // And a valid payload
        String payload = new JsonObject()
                .put("errata_id", 12345)
                .put("errata_status", "QE")
                .encode();
        Message<byte[]> message = mockMessage(payload, "errata.activity.status");

        // When processed
        umbAdvisoryHandler.process(message);

        // Then business logic should be skipped, but message acked
        verify(advisoryHandler, never()).requestGenerations(anyString());
        verify(message).ack();
    }

    private Message<byte[]> mockMessage(String payload, String subject) {
        Message<byte[]> message = mock(Message.class);


        lenient().when(message.getPayload()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        lenient().when(message.ack()).thenReturn(CompletableFuture.completedFuture(null));

        IncomingAmqpMetadata metadata = mock(IncomingAmqpMetadata.class);
        JsonObject properties = new JsonObject().put("subject", subject);


        lenient().when(metadata.getProperties()).thenReturn(properties);

        lenient().when(message.getMetadata(IncomingAmqpMetadata.class)).thenReturn(Optional.of(metadata));

        return message;
    }
}
