package com.dianping.cat.status.model.entity;


import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

import java.util.ArrayList;
import java.util.List;

public class DiskInfo extends BaseEntity<DiskInfo> {
    private List<DiskVolumeInfo> diskVolumes = new ArrayList<DiskVolumeInfo>();

    public DiskInfo() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitDisk(this);
    }

    public DiskInfo addDiskVolume(DiskVolumeInfo diskVolume) {
        diskVolumes.add(diskVolume);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiskInfo) {
            DiskInfo _o = (DiskInfo) obj;
            List<DiskVolumeInfo> diskVolumes = _o.getDiskVolumes();

            return (this.diskVolumes == diskVolumes || this.diskVolumes != null && this.diskVolumes.equals(diskVolumes));
        }

        return false;
    }

    public DiskVolumeInfo findDiskVolume(String id) {
        for (DiskVolumeInfo diskVolume : diskVolumes) {
            if (!diskVolume.getId().equals(id)) {
                continue;
            }

            return diskVolume;
        }

        return null;
    }

    public List<DiskVolumeInfo> getDiskVolumes() {
        return diskVolumes;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (diskVolumes == null ? 0 : diskVolumes.hashCode());

        return hash;
    }

    @Override
    public void mergeAttributes(DiskInfo other) {
    }

    public boolean removeDiskVolume(String id) {
        int len = diskVolumes.size();

        for (int i = 0; i < len; i++) {
            DiskVolumeInfo diskVolume = diskVolumes.get(i);

            if (!diskVolume.getId().equals(id)) {
                continue;
            }

            diskVolumes.remove(i);
            return true;
        }

        return false;
    }

}
