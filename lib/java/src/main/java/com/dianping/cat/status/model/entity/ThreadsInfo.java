package com.dianping.cat.status.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThreadsInfo extends BaseEntity<ThreadsInfo> {
    private int count;
    private int daemonCount;
    private int peekCount;
    private int totalStartedCount;
    private int catThreadCount;
    private int pigeonThreadCount;
    private int httpThreadCount;
    private String dump;

    public ThreadsInfo() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitThread(this);
    }

    @Override
    public void mergeAttributes(ThreadsInfo other) {
        count = other.getCount();

        daemonCount = other.getDaemonCount();

        peekCount = other.getPeekCount();

        totalStartedCount = other.getTotalStartedCount();

        catThreadCount = other.getCatThreadCount();

        pigeonThreadCount = other.getPigeonThreadCount();

        httpThreadCount = other.getHttpThreadCount();
    }

    public ThreadsInfo setCatThreadCount(int catThreadCount) {
        this.catThreadCount = catThreadCount;
        return this;
    }

    public ThreadsInfo setCount(int count) {
        this.count = count;
        return this;
    }

    public ThreadsInfo setDaemonCount(int daemonCount) {
        this.daemonCount = daemonCount;
        return this;
    }

    public ThreadsInfo setDump(String dump) {
        this.dump = dump;
        return this;
    }

    public ThreadsInfo setHttpThreadCount(int httpThreadCount) {
        this.httpThreadCount = httpThreadCount;
        return this;
    }

    public ThreadsInfo setPeekCount(int peekCount) {
        this.peekCount = peekCount;
        return this;
    }

    public ThreadsInfo setPigeonThreadCount(int pigeonThreadCount) {
        this.pigeonThreadCount = pigeonThreadCount;
        return this;
    }

    public ThreadsInfo setTotalStartedCount(int totalStartedCount) {
        this.totalStartedCount = totalStartedCount;
        return this;
    }

}
