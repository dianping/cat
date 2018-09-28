package com.dianping.cat.status.model.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

@Data
@EqualsAndHashCode(callSuper = true)
public class GcInfo extends BaseEntity<GcInfo> {
    private String name;
    private long count;
    private long time;

    public GcInfo() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitGc(this);
    }

    @Override
    public void mergeAttributes(GcInfo other) {
        if (other.getName() != null) {
            name = other.getName();
        }

        count = other.getCount();

        time = other.getTime();
    }

    public GcInfo setCount(long count) {
        this.count = count;
        return this;
    }

    public GcInfo setName(String name) {
        this.name = name;
        return this;
    }

    public GcInfo setTime(long time) {
        this.time = time;
        return this;
    }

}
