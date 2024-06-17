package uk.ac.york.idk503.performancetest.database.component.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.database.component.Executable;
import uk.ac.york.idk503.performancetest.database.repository.MemoryInfoRepository;
import uk.ac.york.idk503.performancetest.database.repository.ThreadInfoRepository;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@Component
public final class CompletableFutureDataLoad extends Executable {
    private static final Logger LOG = LoggerFactory.getLogger(CompletableFutureDataLoad.class);
    private static final Executor pool = new ForkJoinPool(50);

    @Autowired
    public CompletableFutureDataLoad(final MemoryInfoRepository memoryInfoRepository,
                                     final ThreadInfoRepository threadInfoRepository) {
        super(memoryInfoRepository, threadInfoRepository);
    }

    @Override
    public Map<String, Long> runTestAndReturnCount(final long finishTime) {
        try {
            while(finishTime > System.nanoTime()){
                CompletableFuture.runAsync(() -> loadMemoryData(), pool).get();
                CompletableFuture.runAsync(() -> loadThreadData(), pool).get();
            }
            return getRecordCounts();
        } catch (InterruptedException e) {
            LOG.error("CompletableFutureDataLoad failed {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyMap();
        } catch (Exception e) {
            LOG.error("CompletableFutureDataLoad failed {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}