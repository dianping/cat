package com.dianping.cat.status.model;

import com.dianping.cat.status.model.entity.*;

public interface IVisitor {

    void visitCustomInfo(CustomInfo customInfo);

    void visitDisk(DiskInfo disk);

    void visitDiskVolume(DiskVolumeInfo diskVolume);

    void visitExtension(Extension extension);

    void visitExtensionDetail(ExtensionDetail extensionDetail);

    void visitGc(GcInfo gc);

    void visitMemory(MemoryInfo memory);

    void visitMessage(MessageInfo message);

    void visitOs(OsInfo os);

    void visitRuntime(RuntimeInfo runtime);

    void visitStatus(StatusInfo status);

    void visitThread(ThreadsInfo thread);
}
