package uk.ac.york.idk503.performancetest.results;

public class SummaryData {
    private String concurrencyUtility;
    private int runtimeInSeconds;
    private int totalRecordCount;
    private int heapUsed;
    private int heapUsedPerRecordKb;
    private int heapUsedPerSecondMb;
    private int codeHeapNonNmethodsUsed;
    private int metaspaceUsed;
    private int codeHeapProfiledNmethodsUsed;
    private int compressedClassSpaceUsed;
    private int g1EdenSpaceUsedMb;
    private int g1OldGenUsedKb;
    private int g1SurvivorSpaceUsed;
    private int codeHeapNonProfiledNmethodsUsed;
    private double systemLoadAverageByProcessors;

    public int getCodeHeapNonNmethodsUsedMax() {
        return codeHeapNonNmethodsUsed;
    }

    public void setCodeHeapNonNmethodsUsedMax(int codeHeapNonNmethodsUsed) {
        this.codeHeapNonNmethodsUsed = codeHeapNonNmethodsUsed;
    }

    public int getMetaspaceUsed() {
        return metaspaceUsed;
    }

    public void setMetaspaceUsed(int metaspaceUsed) {
        this.metaspaceUsed = metaspaceUsed;
    }

    public int getCodeHeapProfiledNmethodsUsed() {
        return codeHeapProfiledNmethodsUsed;
    }

    public void setCodeHeapProfiledNmethodsUsed(int codeHeapProfiledNmethodsUsed) {
        this.codeHeapProfiledNmethodsUsed = codeHeapProfiledNmethodsUsed;
    }

    public int getCompressedClassSpaceUsed() {
        return compressedClassSpaceUsed;
    }

    public void setCompressedClassSpaceUsed(int compressedClassSpaceUsed) {
        this.compressedClassSpaceUsed = compressedClassSpaceUsed;
    }

    public int getG1EdenSpaceUsedMb() {
        return g1EdenSpaceUsedMb;
    }

    public void setG1EdenSpaceUsedMb(int g1EdenSpaceUsedMb) {
        this.g1EdenSpaceUsedMb = g1EdenSpaceUsedMb;
    }

    public int getG1OldGenUsedKb() {
        return g1OldGenUsedKb;
    }

    public void setG1OldGenUsedKb(int g1OldGenUsedKb) {
        this.g1OldGenUsedKb = g1OldGenUsedKb;
    }

    public int getG1SurvivorSpaceUsed() {
        return g1SurvivorSpaceUsed;
    }

    public void setG1SurvivorSpaceUsed(int g1SurvivorSpaceUsed) {
        this.g1SurvivorSpaceUsed = g1SurvivorSpaceUsed;
    }

    public int getCodeHeapNonProfiledNmethodsUsed() {
        return codeHeapNonProfiledNmethodsUsed;
    }

    public void setCodeHeapNonProfiledNmethodsUsed(int codeHeapNonProfiledNmethodsUsed) {
        this.codeHeapNonProfiledNmethodsUsed = codeHeapNonProfiledNmethodsUsed;
    }

    public double getSystemLoadAverageByProcessors() {
        return systemLoadAverageByProcessors;
    }

    public void setSystemLoadAverageByProcessors(double systemLoadAverageByProcessors) {
        this.systemLoadAverageByProcessors = systemLoadAverageByProcessors;
    }

    public String getConcurrencyUtility() {
        return concurrencyUtility;
    }

    public void setConcurrencyUtility(String concurrencyUtility) {
        this.concurrencyUtility = concurrencyUtility;
    }

    public int getRuntimeInSeconds() {
        return runtimeInSeconds;
    }

    public void setRuntimeInSeconds(int runtimeInSeconds) {
        this.runtimeInSeconds = runtimeInSeconds;
    }

    public int getTotalRecordCount() {
        return totalRecordCount;
    }

    public void setTotalRecordCount(int totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public int getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(int heapUsed) {
        this.heapUsed = heapUsed;
    }

    public int getHeapUsedPerRecordKb() {
        return heapUsedPerRecordKb;
    }

    public void setHeapUsedPerRecordKb(int heapUsedPerRecordKb) {
        this.heapUsedPerRecordKb = heapUsedPerRecordKb;
    }

    public int getHeapUsedPerSecondMb() {
        return heapUsedPerSecondMb;
    }

    public void setHeapUsedPerSecondMb(int heapUsedPerSecondMb) {
        this.heapUsedPerSecondMb = heapUsedPerSecondMb;
    }

    @Override
    public String toString() {
        return "SummaryData{" +
                "concurrencyUtility='" + concurrencyUtility + '\'' +
                ", runtimeInSeconds=" + runtimeInSeconds +
                ", totalRecordCount=" + totalRecordCount +
                ", heapUsed=" + heapUsed +
                ", heapUsedPerRecordKb=" + heapUsedPerRecordKb +
                ", heapUsedPerSecondMb=" + heapUsedPerSecondMb +
                ", codeHeapNonNmethodsUsed=" + codeHeapNonNmethodsUsed +
                ", metaspaceUsed=" + metaspaceUsed +
                ", codeHeapProfiledNmethodsUsed=" + codeHeapProfiledNmethodsUsed +
                ", compressedClassSpaceUsed=" + compressedClassSpaceUsed +
                ", g1EdenSpaceUsed=" + g1EdenSpaceUsedMb +
                ", g1OldGenUsed=" + g1OldGenUsedKb +
                ", g1SurvivorSpaceUsed=" + g1SurvivorSpaceUsed +
                ", codeHeapNonProfiledNmethodsUsed=" + codeHeapNonProfiledNmethodsUsed +
                ", systemLoadAverageByProcessors=" + systemLoadAverageByProcessors +
                '}';
    }
}
