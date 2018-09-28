package com.dianping.cat.status.model.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

@Data
@EqualsAndHashCode(callSuper = true)
public class OsInfo extends BaseEntity<OsInfo> {
    private String name;
    private String arch;
    private String version;
    private int availableProcessors;
    private double systemLoadAverage;
    private long processTime;
    private long totalPhysicalMemory;
    private long freePhysicalMemory;
    private long committedVirtualMemory;
    private long totalSwapSpace;
    private long freeSwapSpace;

    public OsInfo() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitOs(this);
    }

    @Override
    public void mergeAttributes(OsInfo other) {
        if (other.getName() != null) {
            name = other.getName();
        }

        if (other.getArch() != null) {
            arch = other.getArch();
        }

        if (other.getVersion() != null) {
            version = other.getVersion();
        }

        availableProcessors = other.getAvailableProcessors();

        systemLoadAverage = other.getSystemLoadAverage();

        processTime = other.getProcessTime();

        totalPhysicalMemory = other.getTotalPhysicalMemory();

        freePhysicalMemory = other.getFreePhysicalMemory();

        committedVirtualMemory = other.getCommittedVirtualMemory();

        totalSwapSpace = other.getTotalSwapSpace();

        freeSwapSpace = other.getFreeSwapSpace();
    }

    public OsInfo setArch(String arch) {
        this.arch = arch;
        return this;
    }

    public OsInfo setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
        return this;
    }

    public OsInfo setCommittedVirtualMemory(long committedVirtualMemory) {
        this.committedVirtualMemory = committedVirtualMemory;
        return this;
    }

    public OsInfo setFreePhysicalMemory(long freePhysicalMemory) {
        this.freePhysicalMemory = freePhysicalMemory;
        return this;
    }

    public OsInfo setFreeSwapSpace(long freeSwapSpace) {
        this.freeSwapSpace = freeSwapSpace;
        return this;
    }

    public OsInfo setName(String name) {
        this.name = name;
        return this;
    }

    public OsInfo setProcessTime(long processTime) {
        this.processTime = processTime;
        return this;
    }

    public OsInfo setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
        return this;
    }

    public OsInfo setTotalPhysicalMemory(long totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
        return this;
    }

    public OsInfo setTotalSwapSpace(long totalSwapSpace) {
        this.totalSwapSpace = totalSwapSpace;
        return this;
    }

    public OsInfo setVersion(String version) {
        this.version = version;
        return this;
    }

}
