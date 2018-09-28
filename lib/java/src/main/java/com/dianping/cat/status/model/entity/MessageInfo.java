package com.dianping.cat.status.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageInfo extends BaseEntity<MessageInfo> {
    private long produced;
    private long overflowed;
    private long bytes;

    public MessageInfo() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitMessage(this);
    }

    @Override
    public void mergeAttributes(MessageInfo other) {
        produced = other.getProduced();

        overflowed = other.getOverflowed();

        bytes = other.getBytes();
    }

    public MessageInfo setBytes(long bytes) {
        this.bytes = bytes;
        return this;
    }

    public MessageInfo setOverflowed(long overflowed) {
        this.overflowed = overflowed;
        return this;
    }

    public MessageInfo setProduced(long produced) {
        this.produced = produced;
        return this;
    }

}
