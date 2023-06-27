package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class AudioMessage implements XBody {
    private String url;
    private short codec;
    private short duration;
    private long stamp;

    public AudioMessage() {
    }

    public AudioMessage(String url, short codec, short duration, short stamp) {
        this.url = url;
        this.stamp = stamp;
        this.codec = codec;
        this.duration = duration;
    }

    public String messageType() {
        return MessageType.audio.name();
    }

    public boolean checkElementsNotNull() {
        return url != null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public short getCodec() {
        return codec;
    }

    public void setCodec(short codec) {
        this.codec = codec;
    }

    public short getDuration() {
        return duration;
    }

    public void setDuration(short duration) {
        this.duration = duration;
    }

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }
}
