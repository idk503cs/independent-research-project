package uk.ac.york.idk503.performancetest.rest.controller;

import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.york.idk503.performancetest.rest.service.MetadataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestMapping("api/metadata")
@RestController
public final class PerformanceTestMetadataController {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceTestMetadataController.class);
    private final MetadataService metadataService;

    @Autowired
    public PerformanceTestMetadataController(final MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping(value = "/default/{id}", produces="application/json")
    public ResponseEntity<Map<String, String>> getMetadata(@PathVariable final int id) {
        final Map<String, String> metadata = new HashMap<>();

        try {
            metadata.putAll(metadataService.getBase64EncodedId(id));
            metadata.putAll(metadataService.getMemoryMetadata());
            metadata.putAll(metadataService.getThreadMetadata());
        } catch (Exception e) {
            LOG.info("Default endpoint failed on id: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(metadata);
        }

        return ResponseEntity.status(HttpStatus.OK).body(metadata);
    }

    @GetMapping(value = "/completablefuture/{id}", produces="application/json")
    public ResponseEntity<Map<String, String>> getCompletableFutureMetadata(@PathVariable final int id) {
        final Map<String, String> metadata = new HashMap<>();

        final CompletableFuture<Map<String, String>> base64EncodedIdCompletableFuture =
                CompletableFuture.supplyAsync(() -> metadataService.getBase64EncodedId(id));
        final CompletableFuture<Map<String, String>> memoryCompletableFuture =
                CompletableFuture.supplyAsync(metadataService::getMemoryMetadata);
        final CompletableFuture<Map<String, String>> threadCompletableFuture =
                CompletableFuture.supplyAsync(metadataService::getThreadMetadata);
        try {
            metadata.putAll(base64EncodedIdCompletableFuture.get());
            metadata.putAll(memoryCompletableFuture.get());
            metadata.putAll(threadCompletableFuture.get());
        } catch (InterruptedException e) {
            LOG.info("Completable future failed on id: {} - {}", id, e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(metadata);
        } catch (Exception e) {
            LOG.info("Completable future failed on id: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(metadata);
        }

        return ResponseEntity.status(HttpStatus.OK).body(metadata);
    }

    @GetMapping(value = "/executorservice/{id}", produces="application/json")
    public ResponseEntity<Map<String, String>> getExecutorServiceMetadata(@PathVariable final int id) {
        final ExecutorService executorService = Executors.newFixedThreadPool(50);
        final Map<String, String> metadata = new HashMap<>();

        final List<Callable<Map<String, String>>> callables = new ArrayList<>();
        callables.add(() -> metadataService.getBase64EncodedId(id));
        callables.add(metadataService::getMemoryMetadata);
        callables.add(metadataService::getThreadMetadata);

        try {
            executorService.invokeAll(callables);
            for (Callable<Map<String, String>> callable : callables) {
                metadata.putAll(callable.call());
            }
        } catch (InterruptedException e) {
            LOG.info("Failed on id: {} - {}", id, e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(metadata);
        } catch (Exception e) {
            LOG.info("Failed on id: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(metadata);
        } finally {
            executorService.shutdown();
            executorService.close();
        }

        return ResponseEntity.status(HttpStatus.OK).body(metadata);
    }

    @GetMapping(value = "/parallelstream/{id}", produces="application/json")
    public ResponseEntity<Map<String, String>> getParallelStreamMetadata(@PathVariable final int id) {
        final Map<String, String> metadata = new HashMap<>();

        try {
            metadata.putAll(
                    Stream.of(metadataService.getBase64EncodedId(id),
                            metadataService.getMemoryMetadata(),
                            metadataService.getThreadMetadata())
                            .flatMap(map -> map.entrySet().stream())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(metadata);
        }

        return ResponseEntity.status(HttpStatus.OK).body(metadata);
    }

    @GetMapping(value = "/structuredconcurrency/{id}", produces="application/json")
    public ResponseEntity<Map<String, String>> getStructuredConcurrencyMetadata(@PathVariable final int id) {
        final Map<String, String> metadata = new HashMap<>();

        try(StructuredTaskScope<Map<String,String>> scope = new StructuredTaskScope<>()){
            final StructuredTaskScope.Subtask<Map<String, String>> base64EncodedIdSubtask = scope.fork(() -> metadataService.getBase64EncodedId(id));
            final StructuredTaskScope.Subtask<Map<String, String>> memorySubtask = scope.fork(metadataService::getMemoryMetadata);
            final StructuredTaskScope.Subtask<Map<String, String>> threadSubtask = scope.fork(metadataService::getThreadMetadata);

            scope.join();
            metadata.putAll(base64EncodedIdSubtask.get());
            metadata.putAll(memorySubtask.get());
            metadata.putAll(threadSubtask.get());

        } catch (InterruptedException e) {
            LOG.error("Structured concurrency metadata endpoint - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body(metadata);
        } catch (Exception e) {
            LOG.error("Structured concurrency metadata endpoint - {}", e.getMessage());
            return ResponseEntity.status(500).body(metadata);
        }

        return ResponseEntity.status(HttpStatus.OK).body(metadata);
    }
}
