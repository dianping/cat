package com.dianping.cat.status.model.transform;

import com.dianping.cat.status.model.IVisitor;
import com.dianping.cat.status.model.entity.*;

public abstract class BaseVisitor implements IVisitor {
    @Override
    public void visitCustomInfo(CustomInfo customInfo) {
    }

    @Override
    public void visitDisk(DiskInfo disk) {
        for (DiskVolumeInfo diskVolume : disk.getDiskVolumes()) {
            visitDiskVolume(diskVolume);
        }
    }

    @Override
    public void visitDiskVolume(DiskVolumeInfo diskVolume) {
    }

    @Override
    public void visitExtension(Extension extension) {
        for (ExtensionDetail extensionDetail : extension.getDetails().values()) {
            visitExtensionDetail(extensionDetail);
        }
    }

    @Override
    public void visitExtensionDetail(ExtensionDetail extensionDetail) {
    }

    @Override
    public void visitGc(GcInfo gc) {
    }

    @Override
    public void visitMemory(MemoryInfo memory) {
        for (GcInfo gc : memory.getGcs()) {
            visitGc(gc);
        }
    }

    @Override
    public void visitMessage(MessageInfo message) {
    }

    @Override
    public void visitOs(OsInfo os) {
    }

    @Override
    public void visitRuntime(RuntimeInfo runtime) {
    }

    @Override
    public void visitStatus(StatusInfo status) {
        if (status.getRuntime() != null) {
            visitRuntime(status.getRuntime());
        }

        if (status.getOs() != null) {
            visitOs(status.getOs());
        }

        if (status.getDisk() != null) {
            visitDisk(status.getDisk());
        }

        if (status.getMemory() != null) {
            visitMemory(status.getMemory());
        }

        if (status.getThread() != null) {
            visitThread(status.getThread());
        }

        if (status.getMessage() != null) {
            visitMessage(status.getMessage());
        }

        for (Extension extension : status.getExtensions().values()) {
            visitExtension(extension);
        }

        for (CustomInfo customInfo : status.getCustomInfos().values()) {
            visitCustomInfo(customInfo);
        }
    }

    @Override
    public void visitThread(ThreadsInfo thread) {
    }
}
