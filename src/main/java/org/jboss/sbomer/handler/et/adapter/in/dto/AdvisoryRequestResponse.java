package org.jboss.sbomer.handler.et.adapter.in.dto;

import java.util.List;

import org.jboss.sbomer.handler.et.core.domain.generation.Generation;
import org.jboss.sbomer.handler.et.core.domain.generation.GenerationRequest;

/**
 * DTO representing the response of the REST API after requesting generations
 * for advisory.
 * 
 */
public record AdvisoryRequestResponse(GenerationRequest generationRequest) {

}
