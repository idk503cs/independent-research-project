package uk.ac.york.idk503.performancetest.mbean;

import java.io.Serializable;

public class CpuMetrics implements Serializable {
    private final double systemLoadAverage;
    private final int availableProcessors;
    private final String name;
    private final String arch;
    private final String version;

    public CpuMetrics(double systemLoadAverage, int availableProcessors, String name, String arch, String version) {
        this.systemLoadAverage = systemLoadAverage;
        this.availableProcessors = availableProcessors;
        this.name = name;
        this.arch = arch;
        this.version = version;
    }

    @Override
    public String toString() {
        return STR."\{systemLoadAverage},\{availableProcessors},\{name},\{arch},\{version}";
    }
}
