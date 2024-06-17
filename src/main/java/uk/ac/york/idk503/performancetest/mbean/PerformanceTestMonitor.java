package uk.ac.york.idk503.performancetest.mbean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.PerformanceTestApplication;
import uk.ac.york.idk503.performancetest.database.component.Executable;
import uk.ac.york.idk503.performancetest.runner.PerformanceTestRunner;
import uk.ac.york.idk503.performancetest.runner.Testable;

import javax.management.*;
import java.lang.management.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class PerformanceTestMonitor implements PerformanceTestMonitorMBean {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceTestMonitor.class);
    private final PerformanceTestRunner ptRunner;
    private Testable testProgram;

    public PerformanceTestMonitor(final PerformanceTestRunner ptRunner) throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        this.ptRunner = ptRunner;

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("uk.ac.york.mbean:type=PerformanceTestMonitor");
        mBeanServer.registerMBean(this, objectName);
    }

    @Override
    public String getTestProgram() {
        return testProgram.getClass().getSimpleName();
    }

    /**
     * @return
     */
    @Override
    public String getArch() {
        return System.getProperty("os.arch");
    }

    /**
     * @return
     */
    @Override
    public String getMachine() {
        return System.getProperty("machine");
    }

    /**
     * @return
     */
    @Override
    public PerformanceMetrics getPerformanceMetrics() {

        while(true) {
            try {
                String simpleName = getTestProgram();
                MemoryMetrics memoryMetrics = getMemoryMetrics();
                CpuMetrics cpuMetrics = getCpuMetrics();
                DatabaseMetrics dbRowsCreatedMetrics = getDBRowsCreatedMetrics();

                return new PerformanceMetrics(simpleName, memoryMetrics, cpuMetrics, dbRowsCreatedMetrics);
            } catch (Exception e) {
                LOG.error("Failed to get performance metrics. Trying again.");
            }
        }
    }

    @Override
    public MemoryMetrics getMemoryMetrics() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        var memoryPoolMetrics = ManagementFactory.getMemoryPoolMXBeans()
                .stream()
                .map(memoryPool ->
                        new MemoryPoolMetrics(memoryPool.getName(),
                                memoryPool.getUsage().getUsed(),
                                memoryPool.getUsage().getCommitted()))
                .toList();

        return new MemoryMetrics(memoryPoolMetrics, heapUsage.getUsed(), heapUsage.getCommitted(),
                nonHeapUsage.getUsed(), nonHeapUsage.getCommitted());
    }

    /**
     * @return
     */
    @Override
    public CpuMetrics getCpuMetrics() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        var systemLoadAverage = operatingSystemMXBean.getSystemLoadAverage();
        var availableProcessors = operatingSystemMXBean.getAvailableProcessors();
        var name = operatingSystemMXBean.getName();
        var arch = operatingSystemMXBean.getArch();
        var version = operatingSystemMXBean.getVersion();

        return new CpuMetrics(systemLoadAverage, availableProcessors, name, arch, version);
    }

    /**
     * @return
     */
    public DatabaseMetrics getDBRowsCreatedMetrics() {
        final Map<String,Long> dbCount = new HashMap<>();
        if(Objects.nonNull(testProgram) && testProgram instanceof Executable executable)  {
            dbCount.putAll((executable).getRecordCounts());
        }
        return new DatabaseMetrics(dbCount);
    }

    @Override
    public void runDatabaseCompletableFuture() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.DATABASE, PerformanceTestRunner.COMPLETABLE_FUTURE);
        ptRunner.runTest(PerformanceTestRunner.DATABASE, PerformanceTestRunner.COMPLETABLE_FUTURE);
    }

    /**
     *
     */
    @Override
    public void runDatabaseExecutorService() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.DATABASE, PerformanceTestRunner.EXECUTOR_SERVICE);
        ptRunner.runTest(PerformanceTestRunner.DATABASE, PerformanceTestRunner.EXECUTOR_SERVICE);
    }

    /**
     *
     */
    @Override
    public void runDatabaseNoConcurrencyUtility() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.DATABASE, PerformanceTestRunner.NO_CONCURRENCY_UTILITIES);
        ptRunner.runTest(PerformanceTestRunner.DATABASE, PerformanceTestRunner.NO_CONCURRENCY_UTILITIES);
    }

    /**
     *
     */
    @Override
    public void runDatabaseParallelStream() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.DATABASE, PerformanceTestRunner.PARALLEL_STREAM);
        ptRunner.runTest(PerformanceTestRunner.DATABASE, PerformanceTestRunner.PARALLEL_STREAM);
    }

    /**
     *
     */
    @Override
    public void runDatabaseStructuredConcurrency() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.DATABASE, PerformanceTestRunner.STRUCTURED_CONCURRENCY);
        ptRunner.runTest(PerformanceTestRunner.DATABASE, PerformanceTestRunner.STRUCTURED_CONCURRENCY);
    }

    /**
     *
     */
    @Override
    public void runRestCompletableFuture() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.REST, PerformanceTestRunner.COMPLETABLE_FUTURE);
        ptRunner.runTest(PerformanceTestRunner.REST, PerformanceTestRunner.COMPLETABLE_FUTURE);
    }

    /**
     *
     */
    @Override
    public void runRestExecutorService() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.REST, PerformanceTestRunner.EXECUTOR_SERVICE);
        ptRunner.runTest(PerformanceTestRunner.REST, PerformanceTestRunner.EXECUTOR_SERVICE);
    }

    /**
     *
     */
    @Override
    public void runRestNoConcurrencyUtility() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.REST, PerformanceTestRunner.NO_CONCURRENCY_UTILITIES);
        ptRunner.runTest(PerformanceTestRunner.REST, PerformanceTestRunner.NO_CONCURRENCY_UTILITIES);
    }

    /**
     *
     */
    @Override
    public void runRestParallelStream() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.REST, PerformanceTestRunner.PARALLEL_STREAM);
        ptRunner.runTest(PerformanceTestRunner.REST, PerformanceTestRunner.PARALLEL_STREAM);
    }

    /**
     *
     */
    @Override
    public void runRestStructuredConcurrency() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.REST, PerformanceTestRunner.STRUCTURED_CONCURRENCY);
        ptRunner.runTest(PerformanceTestRunner.REST, PerformanceTestRunner.STRUCTURED_CONCURRENCY);
    }

    /**
     *
     */
    @Override
    public void runMultistageCompletableFuture() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.COMPLETABLE_FUTURE);
        ptRunner.runTest(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.COMPLETABLE_FUTURE);
    }

    /**
     *
     */
    @Override
    public void runMultistageExecutorService() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.EXECUTOR_SERVICE);
        ptRunner.runTest(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.EXECUTOR_SERVICE);
    }

    /**
     *
     */
    @Override
    public void runMultistageNoConcurrencyUtility() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.NO_CONCURRENCY_UTILITIES);
        ptRunner.runTest(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.NO_CONCURRENCY_UTILITIES);
    }

    /**
     *
     */
    @Override
    public void runMultistageParallelStream() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.PARALLEL_STREAM);
        ptRunner.runTest(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.PARALLEL_STREAM);
    }

    /**
     *
     */
    @Override
    public void runMultistageStructuredConcurrency() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.STRUCTURED_CONCURRENCY);
        ptRunner.runTest(PerformanceTestRunner.MULTISTAGE, PerformanceTestRunner.STRUCTURED_CONCURRENCY);
    }

    /**
     *
     */
    @Override
    public void runSortCompletableFuture() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.SORT, PerformanceTestRunner.COMPLETABLE_FUTURE);
        ptRunner.runTest(PerformanceTestRunner.SORT, PerformanceTestRunner.COMPLETABLE_FUTURE);
    }

    /**
     *
     */
    @Override
    public void runSortExecutorService() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.SORT, PerformanceTestRunner.EXECUTOR_SERVICE);
        ptRunner.runTest(PerformanceTestRunner.SORT, PerformanceTestRunner.EXECUTOR_SERVICE);
    }

    /**
     *
     */
    @Override
    public void runSortNoConcurrencyUtility() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.SORT, PerformanceTestRunner.NO_CONCURRENCY_UTILITIES);
        ptRunner.runTest(PerformanceTestRunner.SORT, PerformanceTestRunner.NO_CONCURRENCY_UTILITIES);
    }

    /**
     *
     */
    @Override
    public void runSortParallelStream() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.SORT, PerformanceTestRunner.PARALLEL_STREAM);
        ptRunner.runTest(PerformanceTestRunner.SORT, PerformanceTestRunner.PARALLEL_STREAM);
    }

    /**
     *
     */
    @Override
    public void runSortStructuredConcurrency() {
        testProgram = ptRunner.getInstance(PerformanceTestRunner.SORT, PerformanceTestRunner.STRUCTURED_CONCURRENCY);
        ptRunner.runTest(PerformanceTestRunner.SORT, PerformanceTestRunner.STRUCTURED_CONCURRENCY);
    }

    /**
     *
     */
    @Override
    public void shutdown() {
        PerformanceTestApplication.shutdown();
    }

    /**
     *
     */
    @Override
    public void terminate() {
        PerformanceTestApplication.terminate();
    }
}
