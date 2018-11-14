package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.transform.DefaultXmlBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatusInfo extends BaseEntity<StatusInfo> {
    private Date timestamp;
    private RuntimeInfo runtime;
    private OsInfo os;
    private DiskInfo disk;
    private MemoryInfo memory;
    private ThreadsInfo thread;
    private MessageInfo message;
    private final Map<String, Extension> extensions = new LinkedHashMap<String, Extension>();
    private final Map<String, CustomInfo> customInfos = new LinkedHashMap<String, CustomInfo>();

    public StatusInfo() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitStatus(this);
    }

    public StatusInfo addCustomInfo(CustomInfo customInfo) {
        customInfos.put(customInfo.getKey(), customInfo);
        return this;
    }

    public StatusInfo addExtension(Extension extension) {
        extensions.put(extension.getId(), extension);
        return this;
    }

    public CustomInfo findCustomInfo(String key) {
        return customInfos.get(key);
    }

    public Extension findExtension(String id) {
        return extensions.get(id);
    }

    public CustomInfo findOrCreateCustomInfo(String key) {
        CustomInfo customInfo = customInfos.get(key);

        if (customInfo == null) {
            synchronized (customInfos) {
                customInfo = customInfos.get(key);

                if (customInfo == null) {
                    customInfo = new CustomInfo(key);
                    customInfos.put(key, customInfo);
                }
            }
        }

        return customInfo;
    }

    public Extension findOrCreateExtension(String id) {
        Extension extension = extensions.get(id);

        if (extension == null) {
            synchronized (extensions) {
                extension = extensions.get(id);

                if (extension == null) {
                    extension = new Extension(id);
                    extensions.put(id, extension);
                }
            }
        }

        return extension;
    }

    @Override
    public void mergeAttributes(StatusInfo other) {
        if (other.getTimestamp() != null) {
            timestamp = other.getTimestamp();
        }
    }

    public boolean removeCustomInfo(String key) {
        if (customInfos.containsKey(key)) {
            customInfos.remove(key);
            return true;
        }

        return false;
    }

    public boolean removeExtension(String id) {
        if (extensions.containsKey(id)) {
            extensions.remove(id);
            return true;
        }

        return false;
    }

    public StatusInfo setDisk(DiskInfo disk) {
        this.disk = disk;
        return this;
    }

    public StatusInfo setMemory(MemoryInfo memory) {
        this.memory = memory;
        return this;
    }

    public StatusInfo setMessage(MessageInfo message) {
        this.message = message;
        return this;
    }

    public StatusInfo setOs(OsInfo os) {
        this.os = os;
        return this;
    }

    public StatusInfo setRuntime(RuntimeInfo runtime) {
        this.runtime = runtime;
        return this;
    }

    public StatusInfo setThread(ThreadsInfo thread) {
        this.thread = thread;
        return this;
    }

    public StatusInfo setTimestamp(java.util.Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return new DefaultXmlBuilder().buildXml(this);
    }
}
