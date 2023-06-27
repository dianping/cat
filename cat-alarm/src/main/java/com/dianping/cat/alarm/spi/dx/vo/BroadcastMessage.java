package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-4-13
 */
public class BroadcastMessage extends AbstractMessage {
    private long fromUid;

    public long getFromUid() {
        return fromUid;
    }

    public void setFromUid(long fromUid) {
        this.fromUid = fromUid;
    }

}
