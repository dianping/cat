package com.dianping.cat.alarm.spi.dx;

import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.alarm.spi.dx.http.AuthAPIUtil;
import com.dianping.cat.alarm.spi.dx.http.MtHttpUtil;
import com.dianping.cat.alarm.spi.dx.vo.BroadcastMessage;
import com.dianping.cat.alarm.spi.dx.vo.KFPushMessage;
import com.dianping.cat.alarm.spi.dx.vo.PushMessage;
import com.dianping.cat.alarm.spi.dx.vo.TextMessage;
import com.dianping.cat.alarm.spi.dx.vo.XBody;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-5-10
 */
public class Pusher {

	private String url;

	private String appkey;

	private String token;

	private long fromUid;

	private long pubUid;

	private short appId;

	private short toAppId;

	private String fromName;

	private int socket_timeout = 2000;

	private int conn_timeout = 1000;

	public void init(String appkey, String token, short appId, short toAppId, long uid, long pubUid, String sender,
	      String baseUrl, int socket_timeout, int conn_timeout) {
		this.appkey = appkey;
		this.token = token;
		this.fromName = sender;
		this.url = baseUrl;
		this.fromUid = uid;
		this.pubUid = pubUid;
		this.appId = appId;
		this.toAppId = toAppId;
		this.socket_timeout = socket_timeout;
		this.conn_timeout = conn_timeout;
	}

	public void setMaxConnections(int connections) {
		MtHttpUtil.setMaxConnections(connections);
	}

	public JSONObject push(String body, String... receivers) {
		return push(buildDefaultTextMessage(body), receivers);
	}

	public JSONObject push(XBody body, String... receivers) {
		return push(body, null, receivers);
	}

	public JSONObject push(XBody body, String extension, String... receivers) {
		return pushWithStamp(body, extension, 0, receivers);
	}

	public JSONObject pushWithStamp(XBody body, long cts, String... receivers) {
		return pushWithStamp(body, null, cts, receivers);
	}

	/**
	 * 系统消息推送
	 *
	 * @param body
	 *           消息内容
	 * @param extension
	 *           扩展属性
	 * @param cts
	 *           时间戳
	 * @param receivers
	 *           接收者列表
	 * @return
	 */
	public JSONObject pushWithStamp(XBody body, String extension, long cts, String... receivers) {
		PushMessage message = PubMessageHelper.constructPushMessage(fromUid, null, receivers, fromName, toAppId, cts,
		      extension, body);
		return execute(url, message);
	}

	public JSONObject push(String body, long... toUids) {
		return push(buildDefaultTextMessage(body), toUids);
	}

	public JSONObject pushWithStamp(String body, long cts, long... toUids) {
		return pushWithStamp(buildDefaultTextMessage(body), cts, toUids);
	}

	public JSONObject push(XBody body, long... toUids) {
		return pushWithStamp(body, null, 0, toUids);
	}

	public JSONObject pushWithStamp(XBody body, long cts, long... toUids) {
		return pushWithStamp(body, null, cts, toUids);
	}

	/**
	 * 系统消息推送
	 *
	 * @param body
	 *           消息内容
	 * @param extension
	 *           扩展属性
	 * @param cts
	 *           时间戳
	 * @param toUids
	 *           接收者id列表
	 * @return
	 */
	public JSONObject pushWithStamp(XBody body, String extension, long cts, long... toUids) {
		PushMessage message = PubMessageHelper.constructPushMessage(fromUid, toUids, null, fromName, toAppId, cts,
		      extension, body);
		return execute(url, message);
	}

	public JSONObject kfPush(long fromUid, String body, String... receivers) {
		return kfPush(fromUid, buildDefaultTextMessage(body), receivers);
	}

	public JSONObject kfPush(long fromUid, String body, long... toUids) {
		return kfPush(fromUid, buildDefaultTextMessage(body), toUids);
	}

	public JSONObject kfPushWithStamp(long fromUid, String body, long cts, long... toUids) {
		return kfPushWithStamp(fromUid, buildDefaultTextMessage(body), cts, toUids);
	}

	public JSONObject kfPush(long fromUid, XBody body, long... toUids) {
		return kfPush(fromUid, null, body, toUids);
	}

	public JSONObject kfPush(long fromUid, String extension, XBody body, long... toUids) {
		return kfPushWithStamp(fromUid, extension, body, 0, toUids);
	}

	/**
	 * 客服消息推送
	 *
	 * @param fromUid
	 *           公众号id
	 * @param body
	 *           消息内容
	 * @param cts
	 *           时间戳
	 * @param toUids
	 *           接收者id列表
	 * @return
	 */
	public JSONObject kfPushWithStamp(long fromUid, XBody body, long cts, long... toUids) {
		return kfPushWithStamp(fromUid, null, body, cts, toUids);
	}

	public JSONObject kfPushWithStamp(long fromUid, String extension, XBody body, long cts, long... toUids) {
		KFPushMessage kfPushMessage = PubMessageHelper.constructKFPushMessage(fromUid, pubUid, toUids, null, fromName,
		      appId, toAppId, cts, extension, body);
		return execute(url, kfPushMessage);
	}

	public JSONObject kfPush(long fromUid, XBody body, String... receivers) {
		return kfPush(fromUid, null, body, receivers);
	}

	public JSONObject kfPush(long fromUid, String extension, XBody body, String... receivers) {
		return kfPushWithStamp(fromUid, extension, body, 0, receivers);
	}

	public JSONObject kfPushWithStamp(long fromUid, XBody body, long cts, String... receivers) {
		return kfPushWithStamp(fromUid, null, body, cts, receivers);
	}

	/**
	 * 客服消息推送
	 *
	 * @param fromUid
	 *           公众号id
	 * @param extension
	 *           扩展属性
	 * @param body
	 *           消息内容
	 * @param cts
	 *           时间戳
	 * @param receivers
	 *           接收者列表
	 * @return
	 */
	public JSONObject kfPushWithStamp(long fromUid, String extension, XBody body, long cts, String... receivers) {
		KFPushMessage kfPushMessage = PubMessageHelper.constructKFPushMessage(fromUid, pubUid, null, receivers, fromName,
		      appId, toAppId, cts, extension, body);
		return execute(url, kfPushMessage);
	}

	public JSONObject broadcast(String body) {
		return broadcast(buildDefaultTextMessage(body));
	}

	public JSONObject broadcast(XBody body) {
		return broadcast(body, null);
	}

	public JSONObject broadcast(XBody body, String extension) {
		return broadcastWithStamp(body, extension, 0);
	}

	public JSONObject broadcastWithStamp(XBody body, long cts) {
		return broadcastWithStamp(body, null, cts);
	}

	/**
	 * 广播消息推送
	 *
	 * @param body
	 *           消息内容
	 * @param extension
	 *           扩展信息
	 * @param cts
	 *           消息创建时间戳
	 * @return
	 */
	public JSONObject broadcastWithStamp(XBody body, String extension, long cts) {
		BroadcastMessage message = PubMessageHelper.constructBroadcastMessage(fromUid, toAppId, cts, extension, body);
		return execute(url, message);
	}

	public JSONObject transmit(byte[] packets, long... toUids) {
		return transmitWithStamp(packets, 0, toUids);
	}

	/**
	 * 透传协议消息推送
	 *
	 * @param packets
	 *           自定义报文
	 * @param cts
	 *           消息时间戳
	 * @param toUids
	 *           消息接收者
	 * @return
	 */
	public JSONObject transmitWithStamp(byte[] packets, long cts, long... toUids) {
		PushMessage message = PubMessageHelper.constructPushMessage(fromUid, toUids, toAppId, cts, packets);
		return execute(url, message);
	}

	private JSONObject execute(String url, Object message) {
		JSONObject result = MtHttpUtil.put(url, JSONObject.toJSONString(message), JSONObject.class, socket_timeout,
		      conn_timeout, AuthAPIUtil.getPutSignHeaders(url, appkey, token));
		return result.getJSONObject("data");
	}

	public TextMessage buildDefaultTextMessage(String body) {
		TextMessage text = new TextMessage();
		text.setBold(true);
		text.setFontSize(12);
		text.setFontName("宋体");
		text.setText(body);
		text.setCipherType(TextMessage.CipherType.NO_CIPHER);
		return text;
	}

	public long getFromUid() {
		return fromUid;
	}

	public void setFromUid(long fromUid) {
		this.fromUid = fromUid;
	}

	public long getPubUid() {
		return pubUid;
	}

	public void setPubUid(long pubUid) {
		this.pubUid = pubUid;
	}

	public short getAppId() {
		return appId;
	}

	public void setAppId(short appId) {
		this.appId = appId;
	}

	public short getToAppId() {
		return toAppId;
	}

	public void setToAppId(short toAppId) {
		this.toAppId = toAppId;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public Pusher withToAppId(short toAppId) {
		this.toAppId = toAppId;
		return this;
	}

	public Pusher withPubUid(long pubUid) {
		this.pubUid = pubUid;
		return this;
	}
}
