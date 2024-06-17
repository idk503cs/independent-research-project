package uk.ac.york.idk503.performancetest.results;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

public class Results {
    private final List<String> testProgram = new ArrayList<>();
    /*
    This metric is related to the Code Cache area. Contains non-method code, e.g. compiler buffers and the bytecode
    interpreter. Cached code is not stored in memory and isn't replaced. Default size 5MB but can be configured using
    -XX:NonNMethodCodeHeapSize. Used contains the quantity of memory used by CodeHeap ‘non-nmethods’.
     */
    private final DescriptiveStatistics codeHeapNonNmethodsUsed = new DescriptiveStatistics();

    private final DescriptiveStatistics codeHeapNonNmethodsCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics metaspaceUsed = new DescriptiveStatistics();
    private final DescriptiveStatistics metaspaceCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics codeHeapProfiledNmethodsUsed = new DescriptiveStatistics();
    private final DescriptiveStatistics codeHeapProfiledNmethodsCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics compressedClassSpaceUsed = new DescriptiveStatistics();
    private final DescriptiveStatistics compressedClassSpaceCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics g1EdenSpaceUsed = new DescriptiveStatistics();
    private final DescriptiveStatistics g1EdenSpaceCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics g1OldGenUsed = new DescriptiveStatistics();
    private final DescriptiveStatistics g1OldGenCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics g1SurvivorSpaceUsed = new DescriptiveStatistics();
    private final DescriptiveStatistics g1SurvivorSpaceCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics codeHeapNonProfiledNmethodsUsed = new DescriptiveStatistics();
    private final DescriptiveStatistics codeHeapNonProfiledNmethodsCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics heapUsed = new DescriptiveStatistics();
    private final DescriptiveStatistics heapCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics nonHeapUsed = new DescriptiveStatistics();
    private final DescriptiveStatistics nonHeapCommitted = new DescriptiveStatistics();
    private final DescriptiveStatistics systemLoadAverage = new DescriptiveStatistics();
    private final DescriptiveStatistics availableProcessors = new DescriptiveStatistics();
    private final List<String> name = new ArrayList<>();
    private final List<String> arch = new ArrayList<>();
    private final List<String> version = new ArrayList<>();
    private final DescriptiveStatistics memoryRecordCount = new DescriptiveStatistics();
    private final DescriptiveStatistics threadsRecordCount = new DescriptiveStatistics();

    public List<String> getTestProgram() {
        return testProgram;
    }

    public DescriptiveStatistics getCodeHeapNonNmethodsUsed() {
        return codeHeapNonNmethodsUsed;
    }

    public DescriptiveStatistics getCodeHeapNonNmethodsCommitted() {
        return codeHeapNonNmethodsCommitted;
    }

    public DescriptiveStatistics getMetaspaceUsed() {
        return metaspaceUsed;
    }

    public DescriptiveStatistics getMetaspaceCommitted() {
        return metaspaceCommitted;
    }

    public DescriptiveStatistics getCodeHeapProfiledNmethodsUsed() {
        return codeHeapProfiledNmethodsUsed;
    }

    public DescriptiveStatistics getCodeHeapProfiledNmethodsCommitted() {
        return codeHeapProfiledNmethodsCommitted;
    }

    public DescriptiveStatistics getCompressedClassSpaceUsed() {
        return compressedClassSpaceUsed;
    }

    public DescriptiveStatistics getCompressedClassSpaceCommitted() {
        return compressedClassSpaceCommitted;
    }

    public DescriptiveStatistics getG1EdenSpaceUsed() {
        return g1EdenSpaceUsed;
    }

    public DescriptiveStatistics getG1EdenSpaceCommitted() {
        return g1EdenSpaceCommitted;
    }

    public DescriptiveStatistics getG1OldGenUsed() {
        return g1OldGenUsed;
    }

    public DescriptiveStatistics getG1OldGenCommitted() {
        return g1OldGenCommitted;
    }

    public DescriptiveStatistics getG1SurvivorSpaceUsed() {
        return g1SurvivorSpaceUsed;
    }

    public DescriptiveStatistics getG1SurvivorSpaceCommitted() {
        return g1SurvivorSpaceCommitted;
    }

    public DescriptiveStatistics getCodeHeapNonProfiledNmethodsUsed() {
        return codeHeapNonProfiledNmethodsUsed;
    }

    public DescriptiveStatistics getCodeHeapNonProfiledNmethodsCommitted() {
        return codeHeapNonProfiledNmethodsCommitted;
    }

    public DescriptiveStatistics getHeapUsed() {
        return heapUsed;
    }

    public DescriptiveStatistics getHeapCommitted() {
        return heapCommitted;
    }

    public DescriptiveStatistics getNonHeapUsed() {
        return nonHeapUsed;
    }

    public DescriptiveStatistics getNonHeapCommitted() {
        return nonHeapCommitted;
    }

    public DescriptiveStatistics getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public DescriptiveStatistics getAvailableProcessors() {
        return availableProcessors;
    }

    public List<String> getName() {
        return name;
    }

    public List<String> getArch() {
        return arch;
    }

    public List<String> getVersion() {
        return version;
    }

    public DescriptiveStatistics getMemoryRecordCount() {
        return memoryRecordCount;
    }

    public DescriptiveStatistics getThreadsRecordCount() {
        return threadsRecordCount;
    }
}
