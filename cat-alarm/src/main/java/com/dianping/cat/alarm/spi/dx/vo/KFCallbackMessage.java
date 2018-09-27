package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-5-19
 */
public class KFCallbackMessage extends CallbackMessage {

    private long pubUid;

    public long getPubUid() {
        return pubUid;
    }

    public void setPubUid(long pubUid) {
        this.pubUid = pubUid;
    }
}
