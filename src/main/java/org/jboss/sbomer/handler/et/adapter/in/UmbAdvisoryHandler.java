package org.jboss.sbomer.handler.et.adapter.in;

import jakarta.validation.constraints.NotEmpty;
import org.jboss.sbomer.handler.et.core.port.api.AdvisoryHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Handler for processing advisory updates messages received via UMB.
 * 
 * TODO: Implement the UMB handling and triggering of advisory processing.
 */
@ApplicationScoped
public class UmbAdvisoryHandler {

    private AdvisoryHandler advisoryHandler;

    @Inject
    UmbAdvisoryHandler(AdvisoryHandler advisoryHandler) {
        this.advisoryHandler = advisoryHandler;
    }

    public void requestAdvisory() {
        // TODO listen to UMB for advisory update message and request depending on what phase gets triggered
    }

}
