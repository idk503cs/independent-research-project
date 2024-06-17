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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public final class ExecutorServiceDataLoad extends Executable {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceDataLoad.class);

    @Autowired
    public ExecutorServiceDataLoad(final MemoryInfoRepository memoryInfoRepository,
                                   final ThreadInfoRepository threadInfoRepository) {
        super(memoryInfoRepository, threadInfoRepository);
    }

    @Override
    public Map<String, Long> runTestAndReturnCount(final long finishTime) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(50)){
            while (finishTime > System.nanoTime()) {
                executorService.submit(() -> loadMemoryData()).get();
                executorService.submit(() -> loadThreadData()).get();
            }

            return getRecordCounts();
        } catch (InterruptedException e) {
            LOG.error("ExecutorServiceDataLoad failed {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyMap();
        } catch (Exception e) {
            LOG.error("ExecutorServiceDataLoad failed {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}