package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class ImageMessage implements XBody {
    private String thumbnail;
    private String normal;
    private String original;

    public ImageMessage() {
    }

    public ImageMessage(String thumbnail, String normal, String original) {
        this.thumbnail = thumbnail;
        this.normal = normal;
        this.original = original;
    }

    public String messageType() {
        return MessageType.image.name();
    }

    public boolean checkElementsNotNull() {
        return thumbnail != null && normal != null && original != null;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }
}
