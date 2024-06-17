package uk.ac.york.idk503.performancetest.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.database.component.loader.*;
import uk.ac.york.idk503.performancetest.multistage.*;
import uk.ac.york.idk503.performancetest.rest.service.*;
import uk.ac.york.idk503.performancetest.sort.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static uk.ac.york.idk503.performancetest.PerformanceTestApplication.shutdown;

@Component
public final class PerformanceTestRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceTestRunner.class);
    public static final String COMPLETABLE_FUTURE = "CompletableFuture";
    public static final String EXECUTOR_SERVICE = "ExecutorService";
    public static final String PARALLEL_STREAM = "ParallelStream";
    public static final String STRUCTURED_CONCURRENCY = "StructuredConcurrency";
    public static final String NO_CONCURRENCY_UTILITIES = "NoConcurrencyUtilities";
    public static final String DATABASE = "database";
    public static final String REST = "rest";
    public static final String MULTISTAGE = "multistage";
    public static final String SORT = "sort";
    private final Map<String, Map<String, Testable>> testPrograms;

    @Value("#{environment.testType}")
    private String testType;

    @Value("#{environment.concurrencyUtility}")
    private String concurrencyUtility;

    @Autowired
    public PerformanceTestRunner(final TestableProgram testableProgram) {
        this.testPrograms = Map.of(
                DATABASE, getDatabaseMap(testableProgram.psDataLoad(), testableProgram.scDataLoad(),
                        testableProgram.ncuDataLoad(), testableProgram.esDataLoad(), testableProgram.cfDataLoad()),
                REST, getRestMap(testableProgram.cfService(), testableProgram.esService(),
                        testableProgram.psService(), testableProgram.scService(), testableProgram.ncuService()),
                MULTISTAGE, getMultistageMap(testableProgram.cfMultistage(), testableProgram.scMultistage(),
                        testableProgram.esMultistage(), testableProgram.ncuMultistage(), testableProgram.psMultistage()),
                SORT, getSortMap(testableProgram.cfMergeSort(), testableProgram.esMergeSort(),
                        testableProgram.ncuMergeSort(), testableProgram.psMergeSort(), testableProgram.scMergeSort())
        );
    }

    private static Map<String, Testable> getDatabaseMap(ParallelStreamDataLoad parallelStreamDataLoad,
                                                        StructuredConcurrencyDataLoad structuredConcurrencyDataLoad,
                                                        NoConcurrencyUtilityDataLoad noConcurrencyUtilityDataLoad,
                                                        ExecutorServiceDataLoad executorServiceDataLoad,
                                                        CompletableFutureDataLoad completableFutureDataLoad) {
        return Map.of(
                COMPLETABLE_FUTURE, completableFutureDataLoad,
                EXECUTOR_SERVICE, executorServiceDataLoad,
                PARALLEL_STREAM, parallelStreamDataLoad,
                STRUCTURED_CONCURRENCY, structuredConcurrencyDataLoad,
                NO_CONCURRENCY_UTILITIES, noConcurrencyUtilityDataLoad
        );
    }

    private static Map<String, Testable> getRestMap(CompletableFutureService completableFutureService,
                                                    ExecutorServiceService executorServiceService,
                                                    ParallelStreamService parallelStreamService,
                                                    StructuredConcurrencyService structuredConcurrencyService,
                                                    NoConcurrencyUtilityService noConcurrencyUtilityService) {
        return Map.of(
                COMPLETABLE_FUTURE, completableFutureService,
                EXECUTOR_SERVICE, executorServiceService,
                PARALLEL_STREAM, parallelStreamService,
                STRUCTURED_CONCURRENCY, structuredConcurrencyService,
                NO_CONCURRENCY_UTILITIES, noConcurrencyUtilityService
        );
    }

    private static Map<String, Testable> getMultistageMap(CompletableFutureMultistage completableFutureMultistage,
                                                          StructuredConcurrencyMultistage structuredMultistage,
                                                          ExecutorServiceMultistage executorServiceMultistage,
                                                          NoConcurrencyUtilityMultistage noConcurrencyUtilityMultistage,
                                                          ParallelStreamMultistage parallelStreamMultistage) {
        return Map.of(
                COMPLETABLE_FUTURE, completableFutureMultistage,
                EXECUTOR_SERVICE, executorServiceMultistage,
                PARALLEL_STREAM, parallelStreamMultistage,
                STRUCTURED_CONCURRENCY, structuredMultistage,
                NO_CONCURRENCY_UTILITIES, noConcurrencyUtilityMultistage
        );
    }

    private static Map<String, Testable> getSortMap(CompletableFutureMergeSort completableFutureMergeSort,
                                                    ExecutorServiceMergeSort executorServiceMergeSort,
                                                    NoConcurrencyUtilityMergeSort noConcurrencyUtilityMergeSort,
                                                    ParallelStreamMergeSort parallelStreamMergeSort,
                                                    StructuredConcurrencyMergeSort structuredConcurrencyMergeSort) {
        return Map.of(
                COMPLETABLE_FUTURE, completableFutureMergeSort,
                EXECUTOR_SERVICE, executorServiceMergeSort,
                PARALLEL_STREAM, parallelStreamMergeSort,
                STRUCTURED_CONCURRENCY, structuredConcurrencyMergeSort,
                NO_CONCURRENCY_UTILITIES, noConcurrencyUtilityMergeSort
        );
    }

    public static int[] getRandomArray() {
        final int LOWER_BOUND = Objects.nonNull(System.getProperty("lowerBound")) ?
                Integer.valueOf(System.getProperty("lowerBound")) : 0;
        final int UPPER_BOUND = Objects.nonNull(System.getProperty("upperBound")) ?
                Integer.valueOf(System.getProperty("upperBound")): 1_000_000;
        final int SIZE = Objects.nonNull(System.getProperty("size")) ?
                Integer.valueOf(System.getProperty("size")) : 1_000_000;

        return new Random().ints(SIZE, LOWER_BOUND, UPPER_BOUND).toArray();
    }

    public static void displayMemoryInfo(){
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();

        System.out.println("Heap Memory Usage:");
        System.out.println("  Initial Memory: " + heapMemoryUsage.getInit() / (1024 * 1024) + " MB");
        System.out.println("  Used Memory: " + heapMemoryUsage.getUsed() / (1024 * 1024) + " MB");
        System.out.println("  Committed Memory: " + heapMemoryUsage.getCommitted() / (1024 * 1024) + " MB");
        System.out.println("  Max Memory: " + heapMemoryUsage.getMax() / (1024 * 1024) + " MB");
    }

    @Override
    public void run(String[] args) throws Exception {
        if(Objects.nonNull(testType) && !testType.isBlank() &&
                Objects.nonNull(concurrencyUtility) && !concurrencyUtility.isBlank()) {
            runTest(testType, concurrencyUtility);
            shutdown();
        }
    }

    public void runTest(final String testType, final String concurrencyUtility) {
        LOG.info("Started {} - {}", testType, concurrencyUtility);
        if(testPrograms.get(testType).get(concurrencyUtility).test()) {
            LOG.info("Finished {} - {}", testType, concurrencyUtility);
        } else {
            LOG.error("Failed {} - {}", testType, concurrencyUtility);
        }

        displayMemoryInfo();
    }

    public Testable getInstance(String testType, String concurrencyUtility) {
        return testPrograms.get(testType).get(concurrencyUtility);
    }
}
