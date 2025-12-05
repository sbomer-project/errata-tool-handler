package org.jboss.sbomer.test.unit.et.adapter.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.jboss.sbomer.handler.et.adapter.in.RestAdvisoryHandler;
import org.jboss.sbomer.handler.et.adapter.in.dto.AdvisoryRequest;
import org.jboss.sbomer.handler.et.adapter.in.dto.AdvisoryRequestResponse;
import org.jboss.sbomer.handler.et.core.domain.generation.GenerationRequest;
import org.jboss.sbomer.handler.et.core.port.api.AdvisoryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.openfeature.sdk.Client;

@ExtendWith(MockitoExtension.class)
class RestAdvisoryHandlerTest {

    @Mock
    AdvisoryHandler advisoryHandler;

    @Mock
    Client featureClient;

    @InjectMocks
    RestAdvisoryHandler restAdvisoryHandler;

    @BeforeEach
    void setup() {
        // Default behavior: Feature flag is ENABLED for all tests unless specified otherwise
        lenient().when(featureClient.getBooleanValue(eq("rest.handler.enabled"), anyBoolean()))
                .thenReturn(true);
    }

    @Test
    void shouldProcessRequestWhenEnabled() {
        String advisoryId = "12345";
        AdvisoryRequest requestDto = new AdvisoryRequest(advisoryId);

        GenerationRequest mockDomainResponse = new GenerationRequest("REQ-123", Collections.emptyList(), Collections.emptyList());
        when(advisoryHandler.requestGenerations(advisoryId)).thenReturn(mockDomainResponse);

        AdvisoryRequestResponse response = restAdvisoryHandler.requestAdvisory(requestDto);

        assertNotNull(response);
        assertEquals(mockDomainResponse, response.generationRequest());

        verify(advisoryHandler).requestGenerations(advisoryId);
    }

    @Test
    void shouldReturnNullAndSkipProcessingWhenDisabled() {
        // Given the feature flag is DISABLED
        when(featureClient.getBooleanValue(eq("rest.handler.enabled"), anyBoolean()))
                .thenReturn(false);

        AdvisoryRequest requestDto = new AdvisoryRequest("12345");

        AdvisoryRequestResponse response = restAdvisoryHandler.requestAdvisory(requestDto);

        assertNull(response);
        verify(advisoryHandler, never()).requestGenerations(anyString());
    }
}