package org.jboss.sbomer.handler.et.core.domain.generation;

import org.jboss.sbomer.handler.et.core.domain.publish.Publisher;

import java.util.List;

public record GenerationRequest(String requestId, List<Publisher> publishers, List<Generation> generations) {
}
