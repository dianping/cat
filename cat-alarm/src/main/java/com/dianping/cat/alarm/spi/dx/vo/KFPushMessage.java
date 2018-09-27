package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-4-27
 */
public class KFPushMessage extends PushMessage {

    private long pubUid;

    public long getPubUid() {
        return pubUid;
    }

    public void setPubUid(long pubUid) {
        this.pubUid = pubUid;
    }
}
