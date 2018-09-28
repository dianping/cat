package com.dianping.cat.status.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemoryInfo extends BaseEntity<MemoryInfo> {
    private long max;
    private long total;
    private long free;
    private long heapUsage;
    private long nonHeapUsage;
    private List<GcInfo> gcs = new ArrayList<GcInfo>();

    public MemoryInfo() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitMemory(this);
    }

    public MemoryInfo addGc(GcInfo gc) {
        gcs.add(gc);
        return this;
    }

    @Override
    public void mergeAttributes(MemoryInfo other) {
        max = other.getMax();

        total = other.getTotal();

        free = other.getFree();

        heapUsage = other.getHeapUsage();

        nonHeapUsage = other.getNonHeapUsage();
    }

    public MemoryInfo setFree(long free) {
        this.free = free;
        return this;
    }

    public MemoryInfo setHeapUsage(long heapUsage) {
        this.heapUsage = heapUsage;
        return this;
    }

    public MemoryInfo setMax(long max) {
        this.max = max;
        return this;
    }

    public MemoryInfo setNonHeapUsage(long nonHeapUsage) {
        this.nonHeapUsage = nonHeapUsage;
        return this;
    }

    public MemoryInfo setTotal(long total) {
        this.total = total;
        return this;
    }

}
