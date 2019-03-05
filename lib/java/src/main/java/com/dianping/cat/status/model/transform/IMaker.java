package com.dianping.cat.status.model.transform;

import com.dianping.cat.status.model.entity.*;

public interface IMaker<T> {

    CustomInfo buildCustomInfo(T node);

    DiskInfo buildDisk(T node);

    DiskVolumeInfo buildDiskVolume(T node);

    Extension buildExtension(T node);

    ExtensionDetail buildExtensionDetail(T node);

    GcInfo buildGc(T node);

    MemoryInfo buildMemory(T node);

    MessageInfo buildMessage(T node);

    OsInfo buildOs(T node);

    RuntimeInfo buildRuntime(T node);

    StatusInfo buildStatus(T node);

    ThreadsInfo buildThread(T node);
}
