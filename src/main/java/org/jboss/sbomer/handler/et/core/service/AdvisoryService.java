package org.jboss.sbomer.handler.et.core.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.sbomer.events.common.FailureSpec;
import org.jboss.sbomer.handler.et.core.domain.advisory.Advisory;
import org.jboss.sbomer.handler.et.core.domain.advisory.Build;
import org.jboss.sbomer.handler.et.core.domain.exception.AdvisoryProcessingException;
import org.jboss.sbomer.handler.et.core.domain.generation.Generation;
import org.jboss.sbomer.handler.et.core.domain.generation.GenerationRequest;
import org.jboss.sbomer.handler.et.core.domain.publish.Publisher;
import org.jboss.sbomer.handler.et.core.port.api.AdvisoryHandler;
import org.jboss.sbomer.handler.et.core.port.spi.ErrataTool;
import org.jboss.sbomer.handler.et.core.port.spi.FailureNotifier;
import org.jboss.sbomer.handler.et.core.port.spi.GenerationRequestService;
import org.jboss.sbomer.handler.et.core.port.spi.Koji;
import org.jboss.sbomer.handler.et.core.utility.FailureUtility;
import org.jboss.sbomer.handler.et.core.utility.TsidUtility;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AdvisoryService implements AdvisoryHandler {
    ErrataTool errataTool;
    GenerationRequestService generationRequestService;
    Koji koji;
    FailureNotifier failureNotifier;

    @ConfigProperty(name = "sbomer.publisher.atlas.build.name")
    public String ATLAS_BUILD_PUBLISHER_NAME;
    @ConfigProperty(name = "sbomer.publisher.atlas.build.version")
    public String ATLAS_BUILD_PUBLISHER_VERSION;
    @ConfigProperty(name = "sbomer.publisher.atlas.release.name")
    public String ATLAS_RELEASE_PUBLISHER_NAME;
    @ConfigProperty(name = "sbomer.publisher.atlas.release.version")
    public String ATLAS_RELEASE_PUBLISHER_VERSION;

    @Inject
    public AdvisoryService(ErrataTool errataTool, GenerationRequestService generationRequestService, Koji koji, FailureNotifier failureNotifier) {
        this.errataTool = errataTool;
        this.generationRequestService = generationRequestService;
        this.koji = koji;
        this.failureNotifier = failureNotifier;
    }

    @Override
    public GenerationRequest requestGenerations(String advisoryId) {
        log.info("Handling advisory: {}...", advisoryId);

        try {
            Advisory advisory = errataTool.getInfo(advisoryId);

            log.debug("Advisory '{}' current status: {}", advisory.id(), advisory.status());
            List<Publisher> publishers = new ArrayList<>();
            if (advisory.status().equals("QE")) {
                publishers.add(new Publisher(ATLAS_BUILD_PUBLISHER_NAME, ATLAS_BUILD_PUBLISHER_VERSION));
                log.debug("Advisory '{}' is QE, adding {} publisher", advisory.id(), ATLAS_BUILD_PUBLISHER_NAME + "-" + ATLAS_BUILD_PUBLISHER_VERSION);
            } else if (advisory.status().equals("SHIPPED_LIVE")) {
                publishers.add(new Publisher(ATLAS_RELEASE_PUBLISHER_NAME, ATLAS_RELEASE_PUBLISHER_VERSION));
                log.debug("Advisory '{}' is SHIPPED_LIVE, adding {} publisher", advisory.id(), ATLAS_RELEASE_PUBLISHER_NAME + "-" + ATLAS_RELEASE_PUBLISHER_VERSION);
            }

            List<Generation> generations = new ArrayList<>();

            if (advisory.isTextOnly()) {
                log.info("Advisory '{}' type: text-only", advisory.id());
                // TODO: Handle text-only advisories properly
            } else {
                log.info("Advisory '{}' type: standard", advisory.id());
                generations.addAll(attachedBuildsToGenerationRequests(advisory.id()));
            }

            GenerationRequest generationRequest = new GenerationRequest(
                    TsidUtility.createUniqueGenerationRequestId(),
                    publishers,
                    generations);
            // request the generations
            generationRequestService.requestGenerations(generationRequest);

            log.info("Advisory '{}' handled successfully", advisoryId);
            return generationRequest;

        } catch (Exception e) {
            log.error("Failed to handle advisory '{}' due to an unexpected error: {}", advisoryId, e.getMessage(), e);
            FailureSpec failure = FailureUtility.buildFailureSpecFromException(e);
            // Notify the failure (the source is null, no source event).
            failureNotifier.notify(failure, null, null);
            throw new AdvisoryProcessingException("Failed to process advisory " + advisoryId, e);
        }
    }

    List<Generation> attachedBuildsToGenerationRequests(String advisoryId) {
        List<Build> attachedBuilds = errataTool.fetchBuilds(advisoryId);
        log.debug("Advisory '{}' has {} build(s) attached", advisoryId, attachedBuilds.size());
        return attachedBuilds.stream().map(Generation::fromBuild).toList();
    }
}
