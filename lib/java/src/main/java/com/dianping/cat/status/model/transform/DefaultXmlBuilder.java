package com.dianping.cat.status.model.transform;

import com.dianping.cat.status.model.Constants;
import com.dianping.cat.status.model.IEntity;
import com.dianping.cat.status.model.IVisitor;
import com.dianping.cat.status.model.entity.*;

public class DefaultXmlBuilder implements IVisitor {

    private IVisitor visitor = this;

    private int level;

    private StringBuilder sb;

    private boolean compact;

    public DefaultXmlBuilder() {
        this(false);
    }

    public DefaultXmlBuilder(boolean compact) {
        this(compact, new StringBuilder(4096));
    }

    public DefaultXmlBuilder(boolean compact, StringBuilder sb) {
        this.compact = compact;
        this.sb = sb;
        this.sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
    }

    public String buildXml(IEntity<?> entity) {
        entity.accept(visitor);
        return sb.toString();
    }

    protected void endTag(String name) {
        level--;

        indent();
        sb.append("</").append(name).append(">\r\n");
    }

    protected String escape(Object value) {
        return escape(value, false);
    }

    protected String escape(Object value, boolean text) {
        if (value == null) {
            return null;
        }

        String str = value.toString();
        int len = str.length();
        StringBuilder sb = new StringBuilder(len + 16);

        for (int i = 0; i < len; i++) {
            final char ch = str.charAt(i);

            switch (ch) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    if (!text) {
                        sb.append("&quot;");
                        break;
                    }
                default:
                    sb.append(ch);
                    break;
            }
        }

        return sb.toString();
    }

    protected void indent() {
        if (!compact) {
            for (int i = level - 1; i >= 0; i--) {
                sb.append("   ");
            }
        }
    }

    protected void startTag(String name) {
        startTag(name, false, null);
    }

    protected void startTag(String name, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
        startTag(name, null, closed, dynamicAttributes, nameValues);
    }

    protected void startTag(String name, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
        startTag(name, null, false, dynamicAttributes, nameValues);
    }

    protected void startTag(String name, Object text, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
        indent();

        sb.append('<').append(name);

        int len = nameValues.length;

        for (int i = 0; i + 1 < len; i += 2) {
            Object attrName = nameValues[i];
            Object attrValue = nameValues[i + 1];

            if (attrValue != null) {
                sb.append(' ').append(attrName).append("=\"").append(escape(attrValue)).append('"');
            }
        }

        if (dynamicAttributes != null) {
            for (java.util.Map.Entry<String, String> e : dynamicAttributes.entrySet()) {
                sb.append(' ').append(e.getKey()).append("=\"").append(escape(e.getValue())).append('"');
            }
        }

        if (text != null && closed) {
            sb.append('>');
            sb.append(escape(text, true));
            sb.append("</").append(name).append(">\r\n");
        } else {
            if (closed) {
                sb.append('/');
            } else {
                level++;
            }

            sb.append(">\r\n");
        }
    }

    protected void tagWithText(String name, String text, Object... nameValues) {
        if (text == null) {
            return;
        }

        indent();

        sb.append('<').append(name);

        int len = nameValues.length;

        for (int i = 0; i + 1 < len; i += 2) {
            Object attrName = nameValues[i];
            Object attrValue = nameValues[i + 1];

            if (attrValue != null) {
                sb.append(' ').append(attrName).append("=\"").append(escape(attrValue)).append('"');
            }
        }

        sb.append(">");
        sb.append(escape(text, true));
        sb.append("</").append(name).append(">\r\n");
    }

    protected void element(String name, String text, boolean escape) {
        if (text == null) {
            return;
        }

        indent();

        sb.append('<').append(name).append(">");

        if (escape) {
            sb.append(escape(text, true));
        } else {
            sb.append("<![CDATA[").append(text).append("]]>");
        }

        sb.append("</").append(name).append(">\r\n");
    }

    protected String toString(java.util.Date date, String format) {
        if (date != null) {
            return new java.text.SimpleDateFormat(format).format(date);
        } else {
            return null;
        }
    }

    @Override
    public void visitCustomInfo(CustomInfo customInfo) {
        startTag(Constants.ENTITY_CUSTOMINFO, true, null, Constants.ATTR_KEY, customInfo.getKey(), Constants.ATTR_VALUE, customInfo.getValue());
    }

    @Override
    public void visitDisk(DiskInfo disk) {
        startTag(Constants.ENTITY_DISK, null);

        if (!disk.getDiskVolumes().isEmpty()) {
            for (DiskVolumeInfo diskVolume : disk.getDiskVolumes().toArray(new DiskVolumeInfo[0])) {
                diskVolume.accept(visitor);
            }
        }

        endTag(Constants.ENTITY_DISK);
    }

    @Override
    public void visitDiskVolume(DiskVolumeInfo diskVolume) {
        startTag(Constants.ENTITY_DISK_VOLUME, true, null, Constants.ATTR_ID, diskVolume.getId(), Constants.ATTR_TOTAL, diskVolume.getTotal(), Constants.ATTR_FREE, diskVolume.getFree(), Constants.ATTR_USABLE, diskVolume.getUsable());
    }

    @Override
    public void visitExtension(Extension extension) {
        startTag(Constants.ENTITY_EXTENSION, extension.getDynamicAttributes(), Constants.ATTR_ID, extension.getId());

        element(Constants.ELEMENT_DESCRIPTION, extension.getDescription(), false);

        if (!extension.getDetails().isEmpty()) {
            for (ExtensionDetail extensionDetail : extension.getDetails().values().toArray(new ExtensionDetail[0])) {
                extensionDetail.accept(visitor);
            }
        }

        endTag(Constants.ENTITY_EXTENSION);
    }

    @Override
    public void visitExtensionDetail(ExtensionDetail extensionDetail) {
        startTag(Constants.ENTITY_EXTENSIONDETAIL, true, extensionDetail.getDynamicAttributes(), Constants.ATTR_ID, extensionDetail.getId(), Constants.ATTR_VALUE, extensionDetail.getValue());
    }

    @Override
    public void visitGc(GcInfo gc) {
        startTag(Constants.ENTITY_GC, true, null, Constants.ATTR_NAME, gc.getName(), Constants.ATTR_COUNT, gc.getCount(), Constants.ATTR_TIME, gc.getTime());
    }

    @Override
    public void visitMemory(MemoryInfo memory) {
        startTag(Constants.ENTITY_MEMORY, null, Constants.ATTR_MAX, memory.getMax(), Constants.ATTR_TOTAL, memory.getTotal(), Constants.ATTR_FREE, memory.getFree(), Constants.ATTR_HEAP_USAGE, memory.getHeapUsage(), Constants.ATTR_NON_HEAP_USAGE, memory.getNonHeapUsage());

        if (!memory.getGcs().isEmpty()) {
            for (GcInfo gc : memory.getGcs().toArray(new GcInfo[0])) {
                gc.accept(visitor);
            }
        }

        endTag(Constants.ENTITY_MEMORY);
    }

    @Override
    public void visitMessage(MessageInfo message) {
        startTag(Constants.ENTITY_MESSAGE, true, null, Constants.ATTR_PRODUCED, message.getProduced(), Constants.ATTR_OVERFLOWED, message.getOverflowed(), Constants.ATTR_BYTES, message.getBytes());
    }

    @Override
    public void visitOs(OsInfo os) {
        startTag(Constants.ENTITY_OS, true, null, Constants.ATTR_NAME, os.getName(), Constants.ATTR_ARCH, os.getArch(), Constants.ATTR_VERSION, os.getVersion(), Constants.ATTR_AVAILABLE_PROCESSORS, os.getAvailableProcessors(), Constants.ATTR_SYSTEM_LOAD_AVERAGE, os.getSystemLoadAverage(), Constants.ATTR_PROCESS_TIME, os.getProcessTime(), Constants.ATTR_TOTAL_PHYSICAL_MEMORY, os.getTotalPhysicalMemory(), Constants.ATTR_FREE_PHYSICAL_MEMORY, os.getFreePhysicalMemory(), Constants.ATTR_COMMITTED_VIRTUAL_MEMORY, os.getCommittedVirtualMemory(), Constants.ATTR_TOTAL_SWAP_SPACE, os.getTotalSwapSpace(), Constants.ATTR_FREE_SWAP_SPACE, os.getFreeSwapSpace());
    }

    @Override
    public void visitRuntime(RuntimeInfo runtime) {
        startTag(Constants.ENTITY_RUNTIME, null, Constants.ATTR_START_TIME, runtime.getStartTime(), Constants.ATTR_UP_TIME, runtime.getUpTime(), Constants.ATTR_JAVA_VERSION, runtime.getJavaVersion(), Constants.ATTR_USER_NAME, runtime.getUserName());

        element(Constants.ELEMENT_USER_DIR, runtime.getUserDir(), true);

        element(Constants.ELEMENT_JAVA_CLASSPATH, runtime.getJavaClasspath(), true);

        endTag(Constants.ENTITY_RUNTIME);
    }

    @Override
    public void visitStatus(StatusInfo status) {
        startTag(Constants.ENTITY_STATUS, null, Constants.ATTR_TIMESTAMP, toString(status.getTimestamp(), "yyyy-MM-dd HH:mm:ss.SSS"));

        if (status.getRuntime() != null) {
            status.getRuntime().accept(visitor);
        }

        if (status.getOs() != null) {
            status.getOs().accept(visitor);
        }

        if (status.getDisk() != null) {
            status.getDisk().accept(visitor);
        }

        if (status.getMemory() != null) {
            status.getMemory().accept(visitor);
        }

        if (status.getThread() != null) {
            status.getThread().accept(visitor);
        }

        if (status.getMessage() != null) {
            status.getMessage().accept(visitor);
        }

        if (!status.getExtensions().isEmpty()) {
            for (Extension extension : status.getExtensions().values().toArray(new Extension[0])) {
                extension.accept(visitor);
            }
        }

        if (!status.getCustomInfos().isEmpty()) {
            for (CustomInfo customInfo : status.getCustomInfos().values().toArray(new CustomInfo[0])) {
                customInfo.accept(visitor);
            }
        }

        endTag(Constants.ENTITY_STATUS);
    }

    @Override
    public void visitThread(ThreadsInfo thread) {
        startTag(Constants.ENTITY_THREAD, null, Constants.ATTR_COUNT, thread.getCount(), Constants.ATTR_DAEMON_COUNT, thread.getDaemonCount(), Constants.ATTR_PEEK_COUNT, thread.getPeekCount(), Constants.ATTR_TOTAL_STARTED_COUNT, thread.getTotalStartedCount(), Constants.ATTR_CAT_THREAD_COUNT, thread.getCatThreadCount(), Constants.ATTR_PIGEON_THREAD_COUNT, thread.getPigeonThreadCount(), Constants.ATTR_HTTP_THREAD_COUNT, thread.getHttpThreadCount());

        element(Constants.ELEMENT_DUMP, thread.getDump(), true);

        endTag(Constants.ENTITY_THREAD);
    }
}
