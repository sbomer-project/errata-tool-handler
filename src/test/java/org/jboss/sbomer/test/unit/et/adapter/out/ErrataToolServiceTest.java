package org.jboss.sbomer.test.unit.et.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.sbomer.handler.et.adapter.out.ErrataToolService;
import org.jboss.sbomer.handler.et.adapter.out.errata.ErrataClient;
import org.jboss.sbomer.handler.et.adapter.out.errata.dto.Errata;
import org.jboss.sbomer.handler.et.adapter.out.errata.dto.ErrataBuildList;
import org.jboss.sbomer.handler.et.core.domain.advisory.Build;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class ErrataToolServiceTest {

    @Mock
    ErrataClient errataClient;

    @InjectMocks
    ErrataToolService errataToolService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Helper method to load JSON response from test resources
     * @param type The advisory type (container, rpm, textonly)
     * @param descriptiveName The descriptive directory name
     * @param advisoryId The advisory ID
     * @param endpoint The endpoint type (errata or builds)
     */
    private <T> T loadJsonResponse(String type, String descriptiveName, String advisoryId, String endpoint, Class<T> clazz) throws IOException {
        String filename = String.format("%s_%s.json", advisoryId, endpoint);
        String path = String.format("src/test/resources/errata-responses/%s/%s/%s", type, descriptiveName, filename);
        String content = Files.readString(Path.of(path));
        return objectMapper.readValue(content, clazz);
    }

    /**
     * Test data provider for parameterized tests.
     * Leverages the fixed directory structure: errata-responses/{type}/{example-advisory-N}/{advisoryId}_{endpoint}.json
     *
     * Returns: type, descriptiveName, advisoryId, expectedBuildCount, expectedBuildType, expectedFirstBuildId, expectedFirstNvr
     */
    static Stream<Arguments> advisoryTestData() {
        return Stream.of(
            // Container advisories - example-advisory-1 (157826)
            Arguments.of("container", "example-advisory-1", "157826", 11, "CONTAINER_IMAGE", 3925725L,
                "fuse-java-openshift-jdk17-rhel-8-container-1.13-15.1767882203"),

            // Container advisories - example-advisory-2 (152887) - Large advisory with many builds
            Arguments.of("container", "example-advisory-2", "152887", 191, "CONTAINER_IMAGE", null, null),

            // Container advisories - example-advisory-3 (157751)
            Arguments.of("container", "example-advisory-3", "157751", 2, "CONTAINER_IMAGE", 3924690L,
                "jboss-eap8-openjdk21-builder-openshift-container-1.0.1.GA-10.1767793526"),

            // RPM advisories - example-advisory-1 (157548)
            Arguments.of("rpm", "example-advisory-1", "157548", 2, "RPM", 3918400L, "spice-client-win-8.10-3.el8_8.1"),

            // Text-only advisories - example-advisory-1 (155577) - No builds expected
            Arguments.of("textonly", "example-advisory-1", "155577", 0, null, null, null),

            // Text-only advisories - example-advisory-2 (157587) - No builds expected
            Arguments.of("textonly", "example-advisory-2", "157587", 0, null, null, null)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}/{1} - Advisory {2} - Expected {3} builds")
    @MethodSource("advisoryTestData")
    void testFetchBuilds_Parameterized(
            String type,
            String descriptiveName,
            String advisoryId,
            int expectedBuildCount,
            String expectedBuildType,
            Long expectedFirstBuildId,
            String expectedFirstNvr) throws IOException {

        // Given - Load mock responses from JSON files using the fixed directory structure
        Errata mockErrata = loadJsonResponse(type, descriptiveName, advisoryId, "errata", Errata.class);
        ErrataBuildList mockBuildList = loadJsonResponse(type, descriptiveName, advisoryId, "builds", ErrataBuildList.class);

        // Mock the client calls
        when(errataClient.getErratum(advisoryId)).thenReturn(mockErrata);
        when(errataClient.getBuildsList(advisoryId)).thenReturn(mockBuildList);

        // When
        List<Build> builds = errataToolService.fetchBuilds(advisoryId);

        // Then
        assertNotNull(builds, "Builds list should not be null");
        assertEquals(expectedBuildCount, builds.size(),
            String.format("Expected %d builds for %s advisory %s (%s)", expectedBuildCount, type, advisoryId, descriptiveName));

        // Additional assertions for advisories with builds
        if (expectedBuildCount > 0 && expectedFirstBuildId != null) {
            assertNotNull(builds.get(0), "First build should not be null");
            assertEquals(expectedBuildType, builds.get(0).type(),
                String.format("Expected build type %s for %s advisory", expectedBuildType, type));
            assertEquals(expectedFirstBuildId, builds.get(0).id(),
                String.format("Expected first build ID %d for advisory %s", expectedFirstBuildId, advisoryId));
            assertEquals(expectedFirstNvr, builds.get(0).nvr(),
                String.format("Expected first build NVR %s for advisory %s", expectedFirstNvr, advisoryId));
        }
    }

    @Test
    void testDetermineBuildType_Docker() {
        // This would test the private method indirectly through fetchBuilds
        // or you could make it package-private for testing
    }

    @Test
    void testDetermineBuildType_Rpm() {
        // This would test the private method indirectly through fetchBuilds
        // or you could make it package-private for testing
    }

    @Test
    void testDetermineBuildType_Unknown() {
        // This would test the private method indirectly through fetchBuilds
        // or you could make it package-private for testing
    }
}

// Made with Bob
