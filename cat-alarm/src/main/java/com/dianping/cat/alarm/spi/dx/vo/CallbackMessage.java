package com.dianping.cat.alarm.spi.dx.vo;

/**
 * 封装个人发送给服务号的消息
 *
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-31
 */
public class CallbackMessage extends AbstractMessage {

    private String fromName;
    private String passport;
    private long fromUid;
    private short appId;
    private long toUid;


    public short getAppId() {
        return appId;
    }

    public void setAppId(short appId) {
        this.appId = appId;
    }

    public long getToUid() {
        return toUid;
    }

    public void setToUid(long toUid) {
        this.toUid = toUid;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public long getFromUid() {
        return fromUid;
    }

    public void setFromUid(long fromUid) {
        this.fromUid = fromUid;
    }
}
