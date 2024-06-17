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

@Component
public final class NoConcurrencyUtilityDataLoad extends Executable {
    private static final Logger LOG = LoggerFactory.getLogger(NoConcurrencyUtilityDataLoad.class);

    @Autowired
    public NoConcurrencyUtilityDataLoad(final MemoryInfoRepository memoryInfoRepository,
                                        final ThreadInfoRepository threadInfoRepository) {
        super(memoryInfoRepository, threadInfoRepository);
    }

    @Override
    public Map<String, Long> runTestAndReturnCount(final long finishTime) {
        try {
            while (finishTime > System.nanoTime()) {
                this.loadThreadData();
                this.loadMemoryData();
            }
            return getRecordCounts();
        } catch (Exception e) {
            LOG.error("NoConcurrencyUtilityDataLoad failed - {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}