package com.dianping.cat.alarm.spi.dx.vo;

import com.alibaba.fastjson.JSONObject;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-4-2
 */
public class AbstractMessage {

	private final static long VALID_PERIOD = 2 * 60 * 1000;

	private String messageType;

	private Object body;

	private long cts;

	private short toAppId;

	private long toCid;

	// 扩展信息
	private String extension;

	public XBody phraseBody() {
		XBody message = null;
		MessageType type = MessageType.getType(messageType);
		switch (type) {
		case text:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), TextMessage.class);
			break;
		case audio:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), AudioMessage.class);
			break;
		case calendar:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), CalendarMessage.class);
			break;
		case emotion:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), EmotionMessage.class);
			break;
		case file:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), FileMessage.class);
			break;
		case gps:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), GPSMessage.class);
			break;
		case image:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), ImageMessage.class);
			break;
		case link:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), LinkMessage.class);
			break;
		case multilink:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), MultiLinkMessage.class);
			break;
		case vcard:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), VCardMessage.class);
			break;
		case video:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), VideoMessage.class);
			break;
		case event:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), EventMessage.class);
			break;
		case custom:
			message = JSONObject.parseObject(JSONObject.toJSONString(body), CustomMessage.class);
			break;
		case transmission:
			break;
		}
		return message;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	public short getToAppId() {
		return toAppId;
	}

	public void setToAppId(short toAppId) {
		this.toAppId = toAppId;
	}

	public long getCts() {
		long currentTime = System.currentTimeMillis();
		if (cts <= 0 || Math.abs(cts - currentTime) >= VALID_PERIOD) {
			cts = currentTime;
		}
		return cts;
	}

	public void setCts(long cts) {
		this.cts = cts;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public long getToCid() {
		return toCid;
	}

	public void setToCid(long toCid) {
		this.toCid = toCid;
	}
}
