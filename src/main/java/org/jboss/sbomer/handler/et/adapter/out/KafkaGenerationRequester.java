package org.jboss.sbomer.handler.et.adapter.out;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.sbomer.handler.et.core.ApplicationConstants;
import org.jboss.sbomer.handler.et.core.domain.generation.Generation;
import org.jboss.sbomer.handler.et.core.domain.generation.GenerationRequest;
import org.jboss.sbomer.handler.et.core.domain.publish.Publisher;
import org.jboss.sbomer.handler.et.core.port.spi.GenerationRequestService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.jboss.sbomer.events.request.RequestsCreated;
import org.jboss.sbomer.events.request.RequestData;
import org.jboss.sbomer.events.common.ContextSpec;
import org.jboss.sbomer.events.common.GenerationRequestSpec;
import org.jboss.sbomer.events.common.Target;
import org.jboss.sbomer.events.common.PublisherSpec;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class KafkaGenerationRequester implements GenerationRequestService {

    @Inject // Make sure to Inject the emitter
    @Channel("requests-created")
    Emitter<RequestsCreated> emitter;

    @Override
    public void requestGenerations(GenerationRequest generationRequest) {
        log.info("Mapping GenerationRequest DTO to RequestsCreated Avro event for requestId: {}", generationRequest.requestId());

        // Map the DTOs to Avro specs
        List<GenerationRequestSpec> avroGenRequests = mapGenerations(generationRequest.generations());
        List<PublisherSpec> avroPublishers = mapPublishers(generationRequest.publishers());

        // Build the context
        ContextSpec context = ContextSpec.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setType("RequestsCreated")
                .setSource(ApplicationConstants.COMPONENT_NAME)
                .setTimestamp(Instant.now())
                .setCorrelationId(generationRequest.requestId()) // Use the requestId for correlation
                .build();

        // Build the event data
        RequestData data = RequestData.newBuilder()
                .setRequestId(generationRequest.requestId())
                .setGenerationRequests(avroGenRequests)
                .setPublishers(avroPublishers)
                .build();

        // Build the RequestsCreated event
        RequestsCreated event = RequestsCreated.newBuilder()
                .setContext(context)
                .setData(data)
                .build();

        // 5. Send to Kafka
        log.info("Publishing 'RequestsCreated' event for requestId: {}", event.getData().getRequestId());
        emitter.send(event);
        log.debug("Event successfully sent.");
        log.debug("Event payload that was sent: {}", event.toString());
    }

    /**
     * Maps the internal List of Generation DTOs to the Avro List of GenerationRequestSpec.
     */
    private List<GenerationRequestSpec> mapGenerations(List<Generation> generations) {
        if (generations == null) {
            return Collections.emptyList();
        }

        return generations.stream()
                .map(gen -> GenerationRequestSpec.newBuilder()
                        .setGenerationId(gen.id())
                        .setTarget(Target.newBuilder()
                                .setType(gen.target().type())
                                .setIdentifier(gen.target().identifier())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Maps the internal List of Publisher DTOs to the Avro List of PublisherSpec.
     */
    private List<PublisherSpec> mapPublishers(List<Publisher> publishers) {
        if (publishers == null) {
            return Collections.emptyList();
        }

        return publishers.stream()
                .map(pub -> PublisherSpec.newBuilder()
                        .setName(pub.name())
                        .setVersion(pub.version())
                        // .setOptions(...) // Relies on the schema default (empty map)
                        .build())
                .collect(Collectors.toList());
    }
}