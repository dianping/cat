package com.dianping.cat.status.model.transform;

import com.dianping.cat.status.model.entity.*;

public interface ILinker {

    boolean onCustomInfo(StatusInfo parent, CustomInfo customInfo);

    boolean onDisk(StatusInfo parent, DiskInfo disk);

    boolean onDiskVolume(DiskInfo parent, DiskVolumeInfo diskVolume);

    boolean onExtension(StatusInfo parent, Extension extension);

    boolean onExtensionDetail(Extension parent, ExtensionDetail extensionDetail);

    boolean onGc(MemoryInfo parent, GcInfo gc);

    boolean onMemory(StatusInfo parent, MemoryInfo memory);

    boolean onMessage(StatusInfo parent, MessageInfo message);

    boolean onOs(StatusInfo parent, OsInfo os);

    boolean onRuntime(StatusInfo parent, RuntimeInfo runtime);

    boolean onThread(StatusInfo parent, ThreadsInfo thread);
}
