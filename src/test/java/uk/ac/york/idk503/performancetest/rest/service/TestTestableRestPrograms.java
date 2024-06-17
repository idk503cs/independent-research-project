package uk.ac.york.idk503.performancetest.rest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.york.idk503.performancetest.StaticTestConfig;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TestTestableRestPrograms extends StaticTestConfig {
    private MetadataService metadataService = mock(MetadataService.class);

    Testable[] testables = new Testable[]{
            new CompletableFutureService(metadataService),
            new ExecutorServiceService(metadataService),
            new NoConcurrencyUtilityService(metadataService),
            new ParallelStreamService(metadataService),
            new StructuredConcurrencyService(metadataService)
    };

    private AtomicInteger requestCounter = new AtomicInteger(0);

    @BeforeEach
    void setUp() throws InterruptedException {
        doAnswer(invocation -> {
            requestCounter.addAndGet(1);
            return true;
        }).when(metadataService).getRequest(any());
    }

    @Test
    void runTestAndReturnCount() {
        for(Testable testable : testables) {
            requestCounter.set(0);
            testable.test();
            System.out.println(testable.getClass().getSimpleName() + ":" + REST_TARGET);
            assertEquals(REST_TARGET, requestCounter.get());
        }
    }
}
