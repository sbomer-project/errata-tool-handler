package org.jboss.sbomer.handler.et.adapter.out;

import java.util.List;
import java.util.Map;

import org.jboss.sbomer.handler.et.core.port.spi.Koji;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

// TODO implement Koji Service
@ApplicationScoped
@Slf4j
public class KojiService implements Koji {

    @Override
    public Map<Long, String> getImageNames(List<Long> buildIds) {
        throw new UnsupportedOperationException("Unimplemented method 'getImageNames'");
    }

}
