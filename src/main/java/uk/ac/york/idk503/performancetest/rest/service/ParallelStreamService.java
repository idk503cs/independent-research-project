package uk.ac.york.idk503.performancetest.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.net.URI;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.IntStream;

@Component
public final class ParallelStreamService implements Testable {
    private static final Logger LOG = LoggerFactory.getLogger(ParallelStreamService.class);
    private final MetadataService metadataService;

    @Autowired
    public ParallelStreamService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public boolean test() {
        boolean isParallelismLessThanFour = ForkJoinPool.getCommonPoolParallelism() < 4;

        if(isParallelismLessThanFour){
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "2");
            System.setProperty("java.util.concurrent.ForkJoinPool.common.maximumSpares", "2");
        }

        final boolean success = IntStream.range(0, REST_TARGET)
                .parallel()
                .allMatch(i -> {
                    try {
                        final URI uri = URI.create(String.format(BASE_URL, "parallelstream", i));
                        return metadataService.getRequest(uri);
                    } catch (InterruptedException e) {
                        LOG.error("REST Parallel Stream test failed - {}", e.getMessage());
                        Thread.currentThread().interrupt();
                        return false;
                    } catch (NullPointerException | RejectedExecutionException e) {
                        LOG.error("REST Parallel Stream test failed - {}", e.getMessage());
                        return false;
                    }
                });
        if(!success) {
            LOG.error("REST Parallel Stream test failed");
            return false;
        }
        return true;
    }
}
