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
import java.util.concurrent.StructuredTaskScope;

@Component
public final class StructuredConcurrencyDataLoad extends Executable {
    private static final Logger LOG = LoggerFactory.getLogger(StructuredConcurrencyDataLoad.class);

    @Autowired
    public StructuredConcurrencyDataLoad(final MemoryInfoRepository memoryInfoRepository,
                                         final ThreadInfoRepository threadInfoRepository) {
        super(memoryInfoRepository,threadInfoRepository);
    }

    @Override
    public Map<String, Long> runTestAndReturnCount(final long finishTime) {
        try (StructuredTaskScope<Long> scope = new StructuredTaskScope<>()) {
            while(finishTime > System.nanoTime()){
                scope.fork(() -> loadMemoryData());
                scope.fork(() -> loadThreadData());
                scope.join();
            }
            return getRecordCounts();
        } catch (InterruptedException e) {
            LOG.error("StructuredConcurrencyDataLoad failed - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyMap();
        } catch (Exception e) {
            LOG.error("StructuredConcurrencyDataLoad failed - {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}