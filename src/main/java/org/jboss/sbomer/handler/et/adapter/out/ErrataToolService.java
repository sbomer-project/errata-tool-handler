package org.jboss.sbomer.handler.et.adapter.out;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.sbomer.handler.et.adapter.out.errata.ErrataClient;
import org.jboss.sbomer.handler.et.adapter.out.errata.dto.Errata;
import org.jboss.sbomer.handler.et.adapter.out.errata.dto.ErrataBuildList;
import org.jboss.sbomer.handler.et.core.domain.advisory.Advisory;
import org.jboss.sbomer.handler.et.core.domain.advisory.Build;
import org.jboss.sbomer.handler.et.core.port.spi.ErrataTool;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

// TODO currently returns dummy values
@ApplicationScoped
@Slf4j
public class ErrataToolService implements ErrataTool {

    @RestClient
    ErrataClient ec;

    @Override
    public List<Build> fetchBuilds(String advisoryId) {
        log.info("Fetching attached builds for advisory with ID: '{}'...", advisoryId);

        // Fetch erratum to get content types
        Errata erratum = ec.getErratum(advisoryId);
        String buildType = erratum.getDetails()
                .map(details -> determineBuildType(details.getContentTypes()))
                .orElse("UNKNOWN");

        ErrataBuildList erratumBuildList = ec.getBuildsList(advisoryId);

        // Flatten the build list and convert BuildItems to Builds
        List<Build> builds = erratumBuildList.getProductVersions()
                .values()
                .stream()
                .flatMap(productVersionEntry -> productVersionEntry.getBuilds()
                        .stream()
                        .flatMap(build -> build.getBuildItems().values().stream()))
                .map(buildItem -> new Build(
                        buildItem.getId(),
                        buildItem.getNvr(),
                        buildType,
                        // NVR or NEVR?
                        buildItem.getNvr()))
                .collect(Collectors.toList());
        return builds;
    }

    /**
     * Determines the build type based on erratum content types.
     *
     * @param contentTypes List of content types from the erratum
     * @return The build type string (e.g., "CONTAINER_IMAGE", "RPM")
     */
    private String determineBuildType(List<String> contentTypes) {
        if (contentTypes == null || contentTypes.isEmpty()) {
            return "UNKNOWN";
        }

        // Check for container/docker content
        if (contentTypes.stream().anyMatch(type -> type.equalsIgnoreCase("docker") ||
                type.equalsIgnoreCase("container"))) {
            return "CONTAINER_IMAGE";
        }

        // Check for RPM content
        if (contentTypes.stream().anyMatch(type -> type.equalsIgnoreCase("rpm"))) {
            return "RPM";
        }

        // Default to the first content type in uppercase
        return contentTypes.get(0).toUpperCase();
    }

    @Override
    public Advisory getInfo(String advisoryId) {
        // TODO temporary dummy check to test error
        if (advisoryId.equals("456")) {
            throw new RuntimeException("advisoryId 456 is not a valid advisory id");
        }
        return new Advisory(advisoryId, "QE", false);
    }
}
