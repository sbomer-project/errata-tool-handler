package org.jboss.sbomer.handler.et.core.domain.generation;

import org.jboss.sbomer.handler.et.core.domain.advisory.Build;
import org.jboss.sbomer.handler.et.core.utility.TsidUtility;

public record Generation(String id, GenerationTarget target) {

    public static Generation fromBuild(Build build) {
        return new Generation(TsidUtility.createUniqueGenerationId(),
                new GenerationTarget(build.type(), build.identifier()));
    }

}
