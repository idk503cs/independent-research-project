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
import java.util.stream.IntStream;

@Component
public class ParallelStreamDataLoad extends Executable {
    private static final Logger LOG = LoggerFactory.getLogger(ParallelStreamDataLoad.class);

    @Autowired
    public ParallelStreamDataLoad(final MemoryInfoRepository memoryInfoRepository,
                                  final ThreadInfoRepository threadInfoRepository) {
        super(memoryInfoRepository,threadInfoRepository);
    }

    @Override
    public Map<String, Long> runTestAndReturnCount(final long finishTime) {
        try {
            IntStream.generate(() -> 0)
                    .takeWhile(i -> finishTime > System.nanoTime())
                    .parallel()
                    .forEach(x -> {
                        this.loadMemoryData();
                        this.loadThreadData();
                    });

            return getRecordCounts();
        } catch (Exception e) {
            LOG.error("ParallelStreamDataLoad failed - {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}