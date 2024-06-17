package uk.ac.york.idk503.performancetest.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public final class ExecutorServiceService implements Testable {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceService.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(500);
    private final MetadataService metadataService;

    @Autowired
    public ExecutorServiceService(final MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public boolean test() {
        final List<Callable<Boolean>> requests = new ArrayList<>();
        for(int i = 0; i< REST_TARGET; i++){
            final URI uri = URI.create(String.format(BASE_URL, "executorservice", i));
            requests.add(() -> metadataService.getRequest(uri));
        }
        try {
            executorService.invokeAll(requests);
            return true;
        } catch (InterruptedException e) {
            LOG.error("REST ExecutorService test failed - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            LOG.error("REST ExecutorService test failed - {}", e.getMessage());
            return false;
        } finally {
            executorService.shutdown();
            executorService.close();
        }
    }
}
