package org.jboss.sbomer.handler.et.adapter.in;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.sbomer.handler.et.adapter.in.dto.AdvisoryRequest;
import org.jboss.sbomer.handler.et.adapter.in.dto.AdvisoryRequestResponse;
import org.jboss.sbomer.handler.et.core.domain.generation.GenerationRequest;
import org.jboss.sbomer.handler.et.core.port.api.AdvisoryHandler;

import dev.openfeature.sdk.Client;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO: Establish convention to follow for REST endpoint naming across SBOMer
 */
@Slf4j
@Path("/v1/errata-tool")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class RestAdvisoryHandler {

    private AdvisoryHandler advisoryHandler;
    private Client featureClient;

    @ConfigProperty(name = "sbomer.features.rest.enabled.openfeature.default")
    boolean restDefaultEnabled;

    @Inject
    RestAdvisoryHandler(AdvisoryHandler advisoryHandler, Client featureClient) {
        this.advisoryHandler = advisoryHandler;
        this.featureClient = featureClient;
    }

    @POST
    @Path("/generate")
    public AdvisoryRequestResponse requestAdvisory(@Valid AdvisoryRequest advisoryInfo) {
        boolean featureEnabled = featureClient.getBooleanValue("rest.handler.enabled", restDefaultEnabled);
        if (!featureEnabled) {
            log.debug("REST Handler disabled via feature flag.");
            return null;
        }
        log.debug("Submitted a REST request to handle update of advisory info: {}", advisoryInfo);

        // Request generations for the advisory
        GenerationRequest generationRequest = advisoryHandler.requestGenerations(advisoryInfo.advisoryId());

        // Return a response containing generation request details
        return new AdvisoryRequestResponse(generationRequest);
    }

}
