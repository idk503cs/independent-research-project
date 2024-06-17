package uk.ac.york.idk503.performancetest.mbean;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


public final class MemoryMetrics implements Serializable {
    private final LocalDateTime dateTime;
    private final List<MemoryPoolMetrics> memoryPoolMetrics;
    private final long heapUsed;
    private final long heapCommitted;
    private final long nonHeapUsed;
    private final long nonHeapCommitted;

    public MemoryMetrics(final List<MemoryPoolMetrics> memoryPoolMetrics,
                         final long heapUsed, final long heapCommitted,
                         final long nonHeapUsed, final long nonHeapCommitted) {
        this.dateTime = LocalDateTime.now();
        this.memoryPoolMetrics = memoryPoolMetrics;
        this.heapUsed = heapUsed;
        this.heapCommitted = heapCommitted;
        this.nonHeapUsed = nonHeapUsed;
        this.nonHeapCommitted = nonHeapCommitted;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public List<MemoryPoolMetrics> getMemoryPoolMetrics() {
        return memoryPoolMetrics;
    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public long getHeapCommitted() {
        return heapCommitted;
    }

    public long getNonHeapUsed() {
        return nonHeapUsed;
    }

    public long getNonHeapCommitted() {
        return nonHeapCommitted;
    }

    @Override
    public String toString() {
        var memoryPools = memoryPoolMetrics.stream().map(pool -> pool.toString()).collect(Collectors.joining(","));
        return STR."\{memoryPools},\{dateTime},\{heapUsed},\{heapCommitted},\{nonHeapUsed},\{nonHeapCommitted}";
    }
}
