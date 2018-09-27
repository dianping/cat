package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class VCardMessage implements XBody {
    private long uid;
    private String name;
    private String account;

    public VCardMessage() {
    }

    public VCardMessage(long uid, String name, String account) {
        this.uid = uid;
        this.name = name;
        this.account = account;
    }

    public String messageType() {
        return MessageType.vcard.name();
    }

    public boolean checkElementsNotNull() {
        return name != null && account != null;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
