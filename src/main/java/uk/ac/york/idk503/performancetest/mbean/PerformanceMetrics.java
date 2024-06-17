package uk.ac.york.idk503.performancetest.mbean;

import java.io.Serializable;

public class PerformanceMetrics implements Serializable {

    private final String testProgram;
    private final MemoryMetrics memoryMetrics;
    private final CpuMetrics cpuMetrics;
    private final DatabaseMetrics databaseMetrics;

    public PerformanceMetrics(String testProgram, MemoryMetrics memoryMetrics, CpuMetrics cpuMetrics, DatabaseMetrics databaseMetrics) {
        this.testProgram = testProgram;
        this.memoryMetrics = memoryMetrics;
        this.cpuMetrics = cpuMetrics;
        this.databaseMetrics = databaseMetrics;
    }

    public String getTestProgram() {
        return testProgram;
    }

    public MemoryMetrics getMemoryMetrics() {
        return memoryMetrics;
    }

    public CpuMetrics getCpuMetrics() {
        return cpuMetrics;
    }

    public DatabaseMetrics getDatabaseMetrics() {
        return databaseMetrics;
    }

    @Override
    public String toString() {
        return STR."\{testProgram},\{memoryMetrics},\{cpuMetrics},\{databaseMetrics}";
    }
}
