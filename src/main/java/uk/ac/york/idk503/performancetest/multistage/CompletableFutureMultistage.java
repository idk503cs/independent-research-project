package uk.ac.york.idk503.performancetest.multistage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;
import java.util.concurrent.*;

@Component
public final class CompletableFutureMultistage implements Testable {
    private static final Logger LOG = LoggerFactory.getLogger(CompletableFutureMultistage.class);

    public boolean test() {
        final CompletableFuture<TextImage>[] completableFutures = new CompletableFuture[TARGET];
        for(int i = 0; i < TARGET; i++) {
            final int fileName = i;
            completableFutures[i] =
                    CompletableFuture.supplyAsync(() -> new TextImage(String.valueOf(fileName)))
                            .thenComposeAsync(image -> CompletableFuture.supplyAsync(image::resize))
                            .thenComposeAsync(image -> CompletableFuture.supplyAsync(image::addWatermark));
        }

        final CompletableFuture<Void> completableFutureMonitor = CompletableFuture.allOf(completableFutures);

        try {
            completableFutureMonitor.get();
            return true;
        } catch (InterruptedException e) {
            LOG.error("CompletableFuture image processing failure - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException | CancellationException e) {
            LOG.error("CompletableFuture image processing failure - {}", e.getMessage());
            return false;
        }
    }
}
