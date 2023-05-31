package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class FileMessage implements XBody {
    private String id;
    private String url;
    private String name;
    private String format;
    private int size;

    public FileMessage() {
    }

    public FileMessage(String id, String url, String name, String format, int size) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.format = format;
        this.size = size;
    }

    public String messageType() {
        return MessageType.file.name();
    }

    public boolean checkElementsNotNull() {
        return id != null && url != null && name != null & format != null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
