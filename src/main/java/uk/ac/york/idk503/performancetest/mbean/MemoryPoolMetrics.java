package uk.ac.york.idk503.performancetest.mbean;

import java.io.Serializable;

public class MemoryPoolMetrics implements Serializable {
    private final String poolName;
    private final long used;
    private final long committed;

    public MemoryPoolMetrics(String poolName, long used, long committed) {
        this.poolName = poolName;
        this.used = used;
        this.committed = committed;
    }

    public String getPoolName() {
        return poolName;
    }

    public long getUsed() {
        return used;
    }

    public long getCommitted() {
        return committed;
    }

    @Override
    public String toString() {
        return poolName + ',' + used + ',' + committed;
    }
}
