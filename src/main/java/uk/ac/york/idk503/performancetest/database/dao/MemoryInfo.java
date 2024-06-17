package uk.ac.york.idk503.performancetest.database.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public final class MemoryInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long heapMemoryUsage;
    private long nonHeapMemoryUsage;

    public MemoryInfo(final long heapMemoryUsage, final long nonHeapMemoryUsage) {
        this.heapMemoryUsage = heapMemoryUsage;
        this.nonHeapMemoryUsage = nonHeapMemoryUsage;
    }

    protected MemoryInfo() {}

    public Long getId() {
        return id;
    }

    public long getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    public long getNonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    public void setHeapMemoryUsage(final long heapMemoryUsage) {
        this.heapMemoryUsage = heapMemoryUsage;
    }

    public void setNonHeapMemoryUsage(final long nonHeapMemoryUsage) {
        this.nonHeapMemoryUsage = nonHeapMemoryUsage;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryInfo that = (MemoryInfo) o;
        return heapMemoryUsage == that.heapMemoryUsage && nonHeapMemoryUsage == that.nonHeapMemoryUsage && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, heapMemoryUsage, nonHeapMemoryUsage);
    }

    @Override
    public String toString() {
        return STR."MemoryInfo{id=\{id}, heapMemoryUsage=\{heapMemoryUsage}, nonHeapMemoryUsage=\{nonHeapMemoryUsage}\{'}'}";
    }
}
