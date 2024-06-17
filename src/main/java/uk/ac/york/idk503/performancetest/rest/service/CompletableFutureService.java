package uk.ac.york.idk503.performancetest.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Component
public final class CompletableFutureService implements Testable {
    private static final Logger LOG = LoggerFactory.getLogger(CompletableFutureService.class);
    private final MetadataService metadataService;

    @Autowired
    public CompletableFutureService(final MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public boolean test() {
        try(ForkJoinPool pool = new ForkJoinPool(50)) {
            final CompletableFuture<Boolean>[] completableFutures = new CompletableFuture[REST_TARGET];

            for (int i = 0; i < REST_TARGET; i++) {
                final URI uri = URI.create(String.format(BASE_URL, "completablefuture", i));
                completableFutures[i] = CompletableFuture.supplyAsync(() -> {
                    try {
                        return metadataService.getRequest(uri);
                    } catch (InterruptedException e) {
                        LOG.error("Completable future service failure - {}", e.getMessage());
                        Thread.currentThread().interrupt();
                        return false;
                    } catch (Exception e) {
                        LOG.error("Completable future service failure - {}", e.getMessage());
                        return false;
                    }
                }, pool);
            }

            final CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(completableFutures);

            try {
                voidCompletableFuture.get();
                return true;
            } catch (InterruptedException e) {
                LOG.error("REST ExecutorService test failed - {}", e.getMessage());
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                LOG.error("REST ExecutorService test failed - {}", e.getMessage());
                return false;
            }
        }
    }
}
