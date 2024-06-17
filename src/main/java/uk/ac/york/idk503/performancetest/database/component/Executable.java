package uk.ac.york.idk503.performancetest.database.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.york.idk503.performancetest.database.dao.MemoryInfo;
import uk.ac.york.idk503.performancetest.database.dao.ThreadInfo;
import uk.ac.york.idk503.performancetest.database.repository.MemoryInfoRepository;
import uk.ac.york.idk503.performancetest.database.repository.ThreadInfoRepository;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public abstract class Executable implements Testable {
    private static final Logger LOG = LoggerFactory.getLogger(Executable.class);

    private final MemoryInfoRepository memoryInfoRepo;
    private final ThreadInfoRepository threadInfoRepo;

    protected Executable(MemoryInfoRepository memoryInfoRepo, ThreadInfoRepository threadInfoRepo) {
        this.memoryInfoRepo = memoryInfoRepo;
        this.threadInfoRepo = threadInfoRepo;
    }

    protected long loadMemoryData()  {
        final MemoryInfo memoryInfo = new MemoryInfo(
                ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(),
                ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed()
        );

        memoryInfoRepo.saveAndFlush(memoryInfo);
        return memoryInfoRepo.count();
    }

    protected long loadThreadData() {
        final long threadId = ManagementFactory.getThreadMXBean().getAllThreadIds()[0];
        final ThreadInfo threadInfo = new ThreadInfo(
                ManagementFactory.getThreadMXBean().getThreadInfo(threadId).getThreadName(),
                threadId,
                ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime(),
                ManagementFactory.getThreadMXBean().getCurrentThreadUserTime()
        );

        threadInfoRepo.saveAndFlush(threadInfo);
        return threadInfoRepo.count();
    }

    public Map<String,Long> getRecordCounts() {
        return new HashMap<>(Map.of(
                "MEMORY",memoryInfoRepo.count(),
                "THREAD",threadInfoRepo.count()));
    }

    public abstract Map<String,Long> runTestAndReturnCount(final long finishTime);

    private long secondsToNanos(short seconds){
        return seconds * 1_000_000_000L;
    }

    @Override
    public boolean test() {
        final long finishTime = System.nanoTime() + secondsToNanos(RUNTIME_IN_SECONDS);
        final Map<String, Long> results = runTestAndReturnCount(finishTime);
        LOG.info("{} - {}", this.getClass(), results);

        return !results.isEmpty();
    }
}
