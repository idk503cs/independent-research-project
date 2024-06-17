package uk.ac.york.idk503.performancetest.database.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class ThreadInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String threadName;
    private long threadId;
    private long threadCpuTime;
    private long threadUserTime;

    public ThreadInfo(final String threadName, final long threadId, final long threadCpuTime, final long threadUserTime) {
        this.threadName = threadName;
        this.threadId = threadId;
        this.threadCpuTime = threadCpuTime;
        this.threadUserTime = threadUserTime;
    }

    protected ThreadInfo(){}

    public Long getId() {
        return id;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(final String threadName) {
        this.threadName = threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(final long threadId) {
        this.threadId = threadId;
    }

    public long getThreadCpuTime() {
        return threadCpuTime;
    }

    public void setThreadCpuTime(final long threadCpuTime) {
        this.threadCpuTime = threadCpuTime;
    }

    public long getThreadUserTime() {
        return threadUserTime;
    }

    public void setThreadUserTime(final long threadUserTime) {
        this.threadUserTime = threadUserTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadInfo that = (ThreadInfo) o;
        return  threadId == that.threadId && threadCpuTime == that.threadCpuTime &&
                threadUserTime == that.threadUserTime && Objects.equals(id, that.id) &&
                Objects.equals(threadName, that.threadName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, threadName, threadId, threadCpuTime, threadUserTime);
    }

    @Override
    public String toString() {
        return STR."ThreadInfo{id=\{id}, threadName='\{threadName}\{'\''}, threadId=\{threadId}, threadCpuTime=\{threadCpuTime}, threadUserTime=\{threadUserTime}\{'}'}";
    }
}
