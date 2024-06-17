package uk.ac.york.idk503.performancetest.mbean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PerformanceTestMonitorClient {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceTestMonitor.class);
    private static String[] amdHost;
    private static int[] amdJmxPort;
    private static int[] amdHttpPort;
    private static String[] armHost;
    private static int[] armJmxPort;
    private static int[] armHttpPort;
    private final List<PerformanceMetrics> performanceMetricsList = new ArrayList<>();
    private final ObjectName objectName;
    private final JMXServiceURL jmxServiceURL;
    private final JMXConnector jmxConnector;
    private final MBeanServerConnection mBeanServerConnection;
    private final PerformanceTestMonitorMBean performanceTestMonitor;
    private final String arch;
    private final String machine;

    public PerformanceTestMonitorClient(String host, int port) {
        try {
            final String jmxUrl = getJmxUrl(host, port);
            this.jmxServiceURL = new JMXServiceURL(jmxUrl);
            this.jmxConnector = JMXConnectorFactory.connect(jmxServiceURL);
            this.mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            this.objectName = new ObjectName("uk.ac.york.mbean:type=PerformanceTestMonitor");
            this.performanceTestMonitor = javax.management.JMX.newMBeanProxy(
                    mBeanServerConnection, objectName, PerformanceTestMonitorMBean.class, true);
            this.arch = this.performanceTestMonitor.getArch();
            this.machine = this.performanceTestMonitor.getMachine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            amdHost = new String[]{args.length > 0 ? args[0] : "localhost", args.length > 6 ? args[6] : "localhost", args.length > 12 ? args[12] : "localhost"};
            amdJmxPort = new int[]{args.length > 1 ? Integer.valueOf(args[1]) : 10000, args.length > 7 ? Integer.valueOf(args[7]) : 10000, args.length > 13 ? Integer.valueOf(args[13]) : 10000};
            amdHttpPort = new int[] {args.length > 2 ? Integer.valueOf(args[2]) : 8080, args.length > 8 ? Integer.valueOf(args[8]) : 8082, args.length > 14 ? Integer.valueOf(args[14]) : 8084};

            armHost = new String[] {args.length > 3 ? args[3] : "localhost", args.length > 9 ? args[9] : "localhost", args.length > 15 ? args[15] : "localhost"};
            armJmxPort = new int[] {args.length > 4 ? Integer.valueOf(args[4]) : 10001, args.length > 10 ? Integer.valueOf(args[10]) : 10000, args.length > 16 ? Integer.valueOf(args[16]) : 10000};
            armHttpPort = new int[] {args.length > 5 ? Integer.valueOf(args[5]) : 8081, args.length > 11 ? Integer.valueOf(args[11]) : 8083, args.length > 17 ? Integer.valueOf(args[17]) : 8085};

        } catch(Exception e) {
            LOG.error("Invalid parameters.{}", Arrays.toString(args));
            throw new RuntimeException(e);
        }

        try(StructuredTaskScope scope = new StructuredTaskScope()){
            List<StructuredTaskScope.Subtask<Boolean>> amdSubtask = new ArrayList<>();
            amdSubtask.add(scope.fork(() -> waitForTestToStart(amdHost[0], amdHttpPort[0])));
            amdSubtask.add(scope.fork(() -> waitForTestToStart(amdHost[1], amdHttpPort[1])));
            amdSubtask.add(scope.fork(() -> waitForTestToStart(amdHost[2], amdHttpPort[2])));

            List<StructuredTaskScope.Subtask<Boolean>> armSubtask = new ArrayList<>();
            armSubtask.add(scope.fork(() -> waitForTestToStart(armHost[0], armHttpPort[0])));
            armSubtask.add(scope.fork(() -> waitForTestToStart(armHost[1], armHttpPort[1])));
            armSubtask.add(scope.fork(() -> waitForTestToStart(armHost[2], armHttpPort[2])));

            scope.join();

            if(amdSubtask.stream().filter(task -> !task.get()).findAny().isPresent() && armSubtask.stream().filter(task -> !task.get()).findAny().isPresent()) {
                LOG.error("Performance test failed to start. ARM: {}, {},{} - AMD: {}, {}, {}", Arrays.toString(armHost), Arrays.toString(armJmxPort), Arrays.toString(armHttpPort), Arrays.toString(amdHost), Arrays.toString(amdJmxPort), Arrays.toString(amdHttpPort));
                return;
            }

            List<StructuredTaskScope.Subtask<Boolean>> amdProcessMbeanSubtask = new ArrayList<>();
            amdProcessMbeanSubtask.add(scope.fork(() -> processMbeanOperations(amdHost[0], amdJmxPort[0], amdHttpPort[0])));
            amdProcessMbeanSubtask.add(scope.fork(() -> processMbeanOperations(amdHost[1], amdJmxPort[1], amdHttpPort[1])));
            amdProcessMbeanSubtask.add(scope.fork(() -> processMbeanOperations(amdHost[2], amdJmxPort[2], amdHttpPort[2])));

            List<StructuredTaskScope.Subtask<Boolean>> armProcessMbeanSubtask = new ArrayList<>();
            armProcessMbeanSubtask.add(scope.fork(() -> processMbeanOperations(armHost[0], armJmxPort[0], armHttpPort[0])));
            armProcessMbeanSubtask.add(scope.fork(() -> processMbeanOperations(armHost[1], armJmxPort[1], armHttpPort[1])));
            armProcessMbeanSubtask.add(scope.fork(() -> processMbeanOperations(armHost[2], armJmxPort[2], armHttpPort[2])));

            scope.join();

            try {
                if(amdProcessMbeanSubtask.stream().filter(task -> !task.get()).findAny().isPresent() && armProcessMbeanSubtask.stream().filter(task -> !task.get()).findAny().isPresent()) {
                    LOG.error("Performance test failed to complete. ARM: {}, {},{} - AMD: {}, {}, {}", Arrays.toString(armHost), Arrays.toString(armJmxPort), Arrays.toString(armHttpPort), Arrays.toString(amdHost), Arrays.toString(amdJmxPort), Arrays.toString(amdHttpPort));
                }
            } catch (Exception e) {
                LOG.error("Performance test failed to complete. ARM: {}, {},{} - AMD: {}, {}, {}", Arrays.toString(armHost), Arrays.toString(armJmxPort), Arrays.toString(armHttpPort), Arrays.toString(amdHost), Arrays.toString(amdJmxPort), Arrays.toString(amdHttpPort));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Boolean processMbeanOperations(String host, int jmxPort, int httpPort) {
        AtomicReference<PerformanceTestMonitorClient> performanceTestMonitorClient =
                new AtomicReference<>(new PerformanceTestMonitorClient(host, jmxPort));

        List<Runnable> runnableMbeanTests = getRunnableMbeanTests(performanceTestMonitorClient);

        for(Runnable runnableMbean : runnableMbeanTests) {
            performanceTestMonitorClient.get().runTest(runnableMbean);

            try {
                waitForTestToStart(host, httpPort);
            } finally {
                performanceTestMonitorClient.set(new PerformanceTestMonitorClient(host, jmxPort));
            }
        }

        try {
            Thread.sleep(30000);
            performanceTestMonitorClient.get().getPerformanceTestMonitor().terminate();
            return true;
        } catch (Exception e) {
            LOG.error("MBean processing failed: {}", e.getMessage());
            return false;
        }
    }

    private static Boolean waitForTestToStart(String host, int port) {

        try {
            String healthcheckEndpoint = new StringBuilder()
                    .append("http://")
                    .append(host)
                    .append(":")
                    .append(port)
                    .append("/actuator/health")
                    .toString();

            ResponseEntity<String> responseEntity = null;
            do {
                try {
                    Thread.sleep(5000);
                    responseEntity = new RestTemplate().getForEntity(healthcheckEndpoint, String.class);
                } catch (Exception e){
                    LOG.error("Test application is not started yet. Wait 5 second and try again.");
                }
            } while (Objects.isNull(responseEntity) || !responseEntity.getStatusCode().equals(HttpStatus.OK));
        } catch (Exception e) {
            LOG.error("Failed while waiting for test to start");
            return false;
        }
        return true;
    }

    private static List<Runnable> getRunnableMbeanTests(AtomicReference<PerformanceTestMonitorClient> performanceTestMonitorClient) {
        List<Runnable> runnableMbeanTests = new ArrayList<>();
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runRestParallelStream());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runRestNoConcurrencyUtility());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runRestCompletableFuture());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runRestExecutorService());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runRestStructuredConcurrency());

        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runDatabaseNoConcurrencyUtility());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runDatabaseCompletableFuture());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runDatabaseExecutorService());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runDatabaseParallelStream());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runDatabaseStructuredConcurrency());

        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runMultistageNoConcurrencyUtility());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runMultistageCompletableFuture());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runMultistageExecutorService());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runMultistageParallelStream());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runMultistageStructuredConcurrency());

        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runSortNoConcurrencyUtility());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runSortCompletableFuture());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runSortExecutorService());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runSortParallelStream());
        runnableMbeanTests.add(() -> performanceTestMonitorClient.get().getPerformanceTestMonitor().runSortStructuredConcurrency());
        return runnableMbeanTests;
    }

    public PerformanceTestMonitorMBean getPerformanceTestMonitor() {
        return performanceTestMonitor;
    }

    public boolean runTest(Runnable runnable) {
        try {
            var isNotComplete = new AtomicBoolean(true);
            var runTest = Thread.startVirtualThread(runnable);
            Thread.startVirtualThread(() -> getPerformanceData(performanceTestMonitor, isNotComplete));

            runTest.join();
            isNotComplete.set(false);
            writeDataFile();

            try {
                performanceTestMonitor.shutdown();
            }catch(Exception e){
                LOG.error("Shutdown performanceTestMonitor: {}",e.getStackTrace().toString());
            }
            return true;
        } catch(Exception e) {
            LOG.error("Test failed: {}",e.getStackTrace().toString());
            return false;
        }
    }

    private void writeDataFile() {
        String fileName = new StringBuilder()
                .append(this.machine)
                .append('-')
                .append(this.arch)
                .append('-')
                .append(performanceTestMonitor.getTestProgram())
                .append('-')
                .append(getDateTimeStamp())
                .append(".csv")
                .toString();

        LOG.info("File Name: {}", fileName);

        if (!performanceMetricsList.isEmpty()) {

            try (FileWriter fileWriter = new FileWriter(fileName)) {
                for (PerformanceMetrics performanceMetrics : performanceMetricsList) {
                    fileWriter.append(performanceMetrics.toString() + '\n');
                }
            } catch (Exception e) {
                LOG.error("Writing file {} failed", fileName);
            }
        } else {
            try {
                File.createTempFile(fileName, ".empty");
            } catch (Exception e) {
                LOG.error("Empty file write failed: {}", e.getStackTrace().toString());
            }
        }
    }

    public String getDateTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss");
        return now.format(outputFormat);
    }

    private void getPerformanceData(PerformanceTestMonitorMBean performanceTestMonitor, AtomicBoolean isNotComplete) {
        while (isNotComplete.get()) {
            try {
                PerformanceMetrics performanceMetrics = performanceTestMonitor.getPerformanceMetrics();
                performanceMetricsList.add(performanceMetrics);
                System.out.println("Checking data " + performanceMetrics.toString());
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getJmxUrl(String host, int port) {

        return new StringBuilder()
                .append("service:jmx:rmi:///jndi/rmi://")
                .append(host)
                .append(":")
                .append(port)
                .append("/jmxrmi")
                .toString();
    }
}
