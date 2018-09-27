package com.dianping.cat.alarm.spi.dx;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-5-10
 */
public class PusherBuilder {

    private String url;
    private String appkey;
    private String token;
    private long fromUid;
    private long pubUid;
    private short toAppid;
    private short appId;
    private String fromName;
    private int socket_timeout = 0;
    private int conn_timeout = 0;
    private int maxConnections;

    public static PusherBuilder defaultBuilder() {
        return new PusherBuilder();
    }

    public PusherBuilder withTargetUrl(String url) {
        this.url = url;
        return this;
    }

    public PusherBuilder withAppkey(String key) {
        this.appkey = key;
        return this;
    }

    public PusherBuilder withApptoken(String token) {
        this.token = token;
        return this;
    }

    public PusherBuilder withFromUid(long fromUid) {
        this.fromUid = fromUid;
        return this;
    }

    public PusherBuilder withPubUid(long pubUid) {
        this.pubUid = pubUid;
        return this;
    }

    public PusherBuilder withToAppid(short toAppid) {
        this.toAppid = toAppid;
        return this;
    }

    public PusherBuilder withFromName(String fromName) {
        this.fromName = fromName;
        return this;
    }

    public PusherBuilder withSocketTimeOut(int timeOut) {
        this.socket_timeout = timeOut;
        return this;
    }

    public PusherBuilder withConnectTimeOut(int timeOut) {
        this.conn_timeout = timeOut;
        return this;
    }

    public PusherBuilder withMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public PusherBuilder withAppId(short appId) {
        this.appId = appId;
        return this;
    }

    public Pusher build() {
        Pusher pusher = new Pusher();
        pusher.init(appkey, token, appId, toAppid, fromUid, pubUid, fromName, url, socket_timeout, conn_timeout);
        if (this.maxConnections != 0) {
            pusher.setMaxConnections(maxConnections);
        }
        return pusher;
    }
}
