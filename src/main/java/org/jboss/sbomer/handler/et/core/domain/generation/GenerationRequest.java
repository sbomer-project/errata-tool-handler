package org.jboss.sbomer.handler.et.core.domain.generation;

import java.util.List;

import org.jboss.sbomer.handler.et.core.domain.publish.Publisher;

public record GenerationRequest(String requestId, List<Publisher> publishers, List<Generation> generations) {
}
