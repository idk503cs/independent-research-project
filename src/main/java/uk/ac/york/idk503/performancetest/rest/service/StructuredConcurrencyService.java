package uk.ac.york.idk503.performancetest.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Component
public final class StructuredConcurrencyService implements Testable {
    private static final Logger LOG = LoggerFactory.getLogger(StructuredConcurrencyService.class);
    private final MetadataService metadataService;

    @Autowired
    public StructuredConcurrencyService(final MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public boolean test() {

        try(var scope = new StructuredTaskScope<Boolean>()){
            final List<StructuredTaskScope.Subtask<Boolean>> subtasks = new ArrayList<>();
            for(int i = 0; i< REST_TARGET; i++){
                final URI uri = URI.create(String.format(BASE_URL, "structuredconcurrency", i));
                subtasks.add(scope.fork(() -> metadataService.getRequest(uri)));
                if(REST_TARGET %1000 == 0){
                    scope.join();
                }
            }
            scope.join();

            return subtasks.stream().filter(subtask -> subtask.get().equals(false)).findAny().isEmpty();
        } catch (InterruptedException e) {
            LOG.error("REST StructuredConcurrency test failed - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            LOG.error("REST StructuredConcurrency test failed - {}", e.getMessage());
            return false;
        }
    }
}
