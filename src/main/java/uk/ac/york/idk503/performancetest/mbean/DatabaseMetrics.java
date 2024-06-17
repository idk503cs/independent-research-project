package uk.ac.york.idk503.performancetest.mbean;

import java.io.Serializable;
import java.util.Map;

public class DatabaseMetrics implements Serializable {
    private final int memory;
    private final int threads;

    public DatabaseMetrics(Map<String, Long> recordCounts) {
        if(recordCounts.isEmpty()) {
            this.memory = 0;
            this.threads = 0;
        } else {
            this.memory = recordCounts.get("MEMORY").intValue();
            this.threads = recordCounts.get("THREAD").intValue();
        }
    }

    public int getMemory() {
        return memory;
    }

    public int getThreads() {
        return threads;
    }

    @Override
    public String toString() {
        return memory + "," + threads;
    }
}
