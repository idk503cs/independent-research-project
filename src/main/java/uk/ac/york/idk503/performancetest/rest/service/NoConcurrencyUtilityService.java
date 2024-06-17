package uk.ac.york.idk503.performancetest.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.net.URI;

@Component
public final class NoConcurrencyUtilityService implements Testable {

    private static final Logger LOG = LoggerFactory.getLogger(NoConcurrencyUtilityService.class);
    private final MetadataService metadataService;

    @Autowired
    public NoConcurrencyUtilityService(final MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public boolean test() {
        try {
            for(int i = 0; i< REST_TARGET; i++){
                final URI uri = URI.create(String.format(BASE_URL, "default", i));
                metadataService.getRequest(uri);
            }
            return true;
        } catch (InterruptedException e) {
            LOG.error("REST no concurrency utilities test failed - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            LOG.error("REST no concurrency utilities test failed - {}", e.getMessage());
            return false;
        }
    }
}
