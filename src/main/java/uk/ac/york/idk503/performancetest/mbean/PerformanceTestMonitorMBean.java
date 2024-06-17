package uk.ac.york.idk503.performancetest.mbean;

public interface PerformanceTestMonitorMBean {
    String getTestProgram();
    String getArch();
    String getMachine();
    PerformanceMetrics getPerformanceMetrics();
    MemoryMetrics getMemoryMetrics();
    CpuMetrics getCpuMetrics();
    DatabaseMetrics getDBRowsCreatedMetrics();
    void shutdown();
    void terminate();

    // Database tests
    void runDatabaseCompletableFuture();
    void runDatabaseExecutorService();
    void runDatabaseNoConcurrencyUtility();
    void runDatabaseParallelStream();
    void runDatabaseStructuredConcurrency();

    // REST tests
    void runRestCompletableFuture();
    void runRestExecutorService();
    void runRestNoConcurrencyUtility();
    void runRestParallelStream();
    void runRestStructuredConcurrency();

    // Multistep tests
    void runMultistageCompletableFuture();
    void runMultistageExecutorService();
    void runMultistageNoConcurrencyUtility();
    void runMultistageParallelStream();
    void runMultistageStructuredConcurrency();

    // Sort tests
    void runSortCompletableFuture();
    void runSortExecutorService();
    void runSortNoConcurrencyUtility();
    void runSortParallelStream();
    void runSortStructuredConcurrency();
}
