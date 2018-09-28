package com.dianping.cat.status.model.entity;


import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

import static com.dianping.cat.status.model.Constants.ATTR_ID;
import static com.dianping.cat.status.model.Constants.ENTITY_DISK_VOLUME;

public class DiskVolumeInfo extends BaseEntity<DiskVolumeInfo> {
    private String id;
    private long total;
    private long free;
    private long usable;

    public DiskVolumeInfo() {
    }

    public DiskVolumeInfo(String id) {
        this.id = id;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitDiskVolume(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiskVolumeInfo) {
            DiskVolumeInfo _o = (DiskVolumeInfo) obj;
            String id = _o.getId();

            return this.id == id || this.id != null && this.id.equals(id);
        }

        return false;
    }

    public long getFree() {
        return free;
    }

    public String getId() {
        return id;
    }

    public long getTotal() {
        return total;
    }

    public long getUsable() {
        return usable;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (id == null ? 0 : id.hashCode());

        return hash;
    }

    @Override
    public void mergeAttributes(DiskVolumeInfo other) {
        assertAttributeEquals(other, ENTITY_DISK_VOLUME, ATTR_ID, id, other.getId());

        total = other.getTotal();

        free = other.getFree();

        usable = other.getUsable();
    }

    public DiskVolumeInfo setFree(long free) {
        this.free = free;
        return this;
    }

    public DiskVolumeInfo setId(String id) {
        this.id = id;
        return this;
    }

    public DiskVolumeInfo setTotal(long total) {
        this.total = total;
        return this;
    }

    public DiskVolumeInfo setUsable(long usable) {
        this.usable = usable;
        return this;
    }

}
