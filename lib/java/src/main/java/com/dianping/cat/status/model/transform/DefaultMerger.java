package com.dianping.cat.status.model.transform;

import com.dianping.cat.status.model.IEntity;
import com.dianping.cat.status.model.IVisitor;
import com.dianping.cat.status.model.entity.*;

import java.util.Stack;

public class DefaultMerger implements IVisitor {
    private Stack<Object> objects = new Stack<Object>();
    private StatusInfo status;

    public DefaultMerger() {
    }

    public DefaultMerger(StatusInfo status) {
        this.status = status;
        objects.push(status);
    }

    public StatusInfo getStatus() {
        return status;
    }

    protected Stack<Object> getObjects() {
        return objects;
    }

    public <T> void merge(IEntity<T> to, IEntity<T> from) {
        objects.push(to);
        from.accept(this);
        objects.pop();
    }

    protected void mergeCustomInfo(CustomInfo to, CustomInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeDisk(DiskInfo to, DiskInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeDiskVolume(DiskVolumeInfo to, DiskVolumeInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeExtension(Extension to, Extension from) {
        to.mergeAttributes(from);
        to.setDescription(from.getDescription());
    }

    protected void mergeExtensionDetail(ExtensionDetail to, ExtensionDetail from) {
        to.mergeAttributes(from);
    }

    protected void mergeGc(GcInfo to, GcInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeMemory(MemoryInfo to, MemoryInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeMessage(MessageInfo to, MessageInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeOs(OsInfo to, OsInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeRuntime(RuntimeInfo to, RuntimeInfo from) {
        to.mergeAttributes(from);
        to.setUserDir(from.getUserDir());
        to.setJavaClasspath(from.getJavaClasspath());
    }

    protected void mergeStatus(StatusInfo to, StatusInfo from) {
        to.mergeAttributes(from);
    }

    protected void mergeThread(ThreadsInfo to, ThreadsInfo from) {
        to.mergeAttributes(from);
        to.setDump(from.getDump());
    }

    @Override
    public void visitCustomInfo(CustomInfo from) {
        CustomInfo to = (CustomInfo) objects.peek();

        mergeCustomInfo(to, from);
        visitCustomInfoChildren(to, from);
    }

    protected void visitCustomInfoChildren(CustomInfo to, CustomInfo from) {
    }

    @Override
    public void visitDisk(DiskInfo from) {
        DiskInfo to = (DiskInfo) objects.peek();

        mergeDisk(to, from);
        visitDiskChildren(to, from);
    }

    protected void visitDiskChildren(DiskInfo to, DiskInfo from) {
        for (DiskVolumeInfo source : from.getDiskVolumes()) {
            DiskVolumeInfo target = to.findDiskVolume(source.getId());

            if (target == null) {
                target = new DiskVolumeInfo(source.getId());
                to.addDiskVolume(target);
            }

            objects.push(target);
            source.accept(this);
            objects.pop();
        }
    }

    @Override
    public void visitDiskVolume(DiskVolumeInfo from) {
        DiskVolumeInfo to = (DiskVolumeInfo) objects.peek();

        mergeDiskVolume(to, from);
        visitDiskVolumeChildren(to, from);
    }

    protected void visitDiskVolumeChildren(DiskVolumeInfo to, DiskVolumeInfo from) {
    }

    @Override
    public void visitExtension(Extension from) {
        Extension to = (Extension) objects.peek();

        mergeExtension(to, from);
        visitExtensionChildren(to, from);
    }

    protected void visitExtensionChildren(Extension to, Extension from) {
        for (ExtensionDetail source : from.getDetails().values()) {
            ExtensionDetail target = to.findExtensionDetail(source.getId());

            if (target == null) {
                target = new ExtensionDetail(source.getId());
                to.addExtensionDetail(target);
            }

            objects.push(target);
            source.accept(this);
            objects.pop();
        }
    }

    @Override
    public void visitExtensionDetail(ExtensionDetail from) {
        ExtensionDetail to = (ExtensionDetail) objects.peek();

        mergeExtensionDetail(to, from);
        visitExtensionDetailChildren(to, from);
    }

    protected void visitExtensionDetailChildren(ExtensionDetail to, ExtensionDetail from) {
    }

    @Override
    public void visitGc(GcInfo from) {
        GcInfo to = (GcInfo) objects.peek();

        mergeGc(to, from);
        visitGcChildren(to, from);
    }

    protected void visitGcChildren(GcInfo to, GcInfo from) {
    }

    @Override
    public void visitMemory(MemoryInfo from) {
        MemoryInfo to = (MemoryInfo) objects.peek();

        mergeMemory(to, from);
        visitMemoryChildren(to, from);
    }

    protected void visitMemoryChildren(MemoryInfo to, MemoryInfo from) {
        for (GcInfo source : from.getGcs()) {
            GcInfo target = null;

            if (target == null) {
                target = new GcInfo();
                to.addGc(target);
            }

            objects.push(target);
            source.accept(this);
            objects.pop();
        }
    }

    @Override
    public void visitMessage(MessageInfo from) {
        MessageInfo to = (MessageInfo) objects.peek();

        mergeMessage(to, from);
        visitMessageChildren(to, from);
    }

    protected void visitMessageChildren(MessageInfo to, MessageInfo from) {
    }

    @Override
    public void visitOs(OsInfo from) {
        OsInfo to = (OsInfo) objects.peek();

        mergeOs(to, from);
        visitOsChildren(to, from);
    }

    protected void visitOsChildren(OsInfo to, OsInfo from) {
    }

    @Override
    public void visitRuntime(RuntimeInfo from) {
        RuntimeInfo to = (RuntimeInfo) objects.peek();

        mergeRuntime(to, from);
        visitRuntimeChildren(to, from);
    }

    protected void visitRuntimeChildren(RuntimeInfo to, RuntimeInfo from) {
    }

    @Override
    public void visitStatus(StatusInfo from) {
        StatusInfo to = (StatusInfo) objects.peek();

        mergeStatus(to, from);
        visitStatusChildren(to, from);
    }

    protected void visitStatusChildren(StatusInfo to, StatusInfo from) {
        if (from.getRuntime() != null) {
            RuntimeInfo target = to.getRuntime();

            if (target == null) {
                target = new RuntimeInfo();
                to.setRuntime(target);
            }

            objects.push(target);
            from.getRuntime().accept(this);
            objects.pop();
        }

        if (from.getOs() != null) {
            OsInfo target = to.getOs();

            if (target == null) {
                target = new OsInfo();
                to.setOs(target);
            }

            objects.push(target);
            from.getOs().accept(this);
            objects.pop();
        }

        if (from.getDisk() != null) {
            DiskInfo target = to.getDisk();

            if (target == null) {
                target = new DiskInfo();
                to.setDisk(target);
            }

            objects.push(target);
            from.getDisk().accept(this);
            objects.pop();
        }

        if (from.getMemory() != null) {
            MemoryInfo target = to.getMemory();

            if (target == null) {
                target = new MemoryInfo();
                to.setMemory(target);
            }

            objects.push(target);
            from.getMemory().accept(this);
            objects.pop();
        }

        if (from.getThread() != null) {
            ThreadsInfo target = to.getThread();

            if (target == null) {
                target = new ThreadsInfo();
                to.setThread(target);
            }

            objects.push(target);
            from.getThread().accept(this);
            objects.pop();
        }

        if (from.getMessage() != null) {
            MessageInfo target = to.getMessage();

            if (target == null) {
                target = new MessageInfo();
                to.setMessage(target);
            }

            objects.push(target);
            from.getMessage().accept(this);
            objects.pop();
        }

        for (Extension source : from.getExtensions().values()) {
            Extension target = to.findExtension(source.getId());

            if (target == null) {
                target = new Extension(source.getId());
                to.addExtension(target);
            }

            objects.push(target);
            source.accept(this);
            objects.pop();
        }

        for (CustomInfo source : from.getCustomInfos().values()) {
            CustomInfo target = to.findCustomInfo(source.getKey());

            if (target == null) {
                target = new CustomInfo(source.getKey());
                to.addCustomInfo(target);
            }

            objects.push(target);
            source.accept(this);
            objects.pop();
        }
    }

    @Override
    public void visitThread(ThreadsInfo from) {
        ThreadsInfo to = (ThreadsInfo) objects.peek();

        mergeThread(to, from);
        visitThreadChildren(to, from);
    }

    protected void visitThreadChildren(ThreadsInfo to, ThreadsInfo from) {
    }
}
