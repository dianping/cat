package com.dianping.cat.alarm.spi.dx.vo;

import java.util.List;

/**
 * 封装需要推送的消息
 *
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-31
 */
public class PushMessage extends AbstractMessage {

	private String pushId;

	private long fromUid;

	private String[] receivers;

	private String fromName;

	private long[] toUids;

	private long appId;

	// 标记消息来源，大于1表示消息来源于公众平台
	private long source;

	// 标记客服号的uid
	private long pubUid;

	private int pushType;

	private List<DispatchMessageId> dispatchMessageIds;

	// 设备类型
	private byte toDeviceTypes;

	public String[] getReceivers() {
		return receivers;
	}

	public void setReceivers(String[] receivers) {
		this.receivers = receivers;
	}

	public long getFromUid() {
		return fromUid;
	}

	public void setFromUid(long fromUid) {
		this.fromUid = fromUid;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public long[] getToUids() {
		return toUids;
	}

	public void setToUids(long[] toUids) {
		this.toUids = toUids;
	}

	public long getAppId() {
		return appId;
	}

	public void setAppId(long appId) {
		this.appId = appId;
	}

	public long getSource() {
		return source;
	}

	public void setSource(long source) {
		this.source = source;
	}

	public long getPubUid() {
		return pubUid;
	}

	public void setPubUid(long pubUid) {
		this.pubUid = pubUid;
	}

	public int getPushType() {
		return pushType;
	}

	public void setPushType(int pushType) {
		this.pushType = pushType;
	}

	public List<DispatchMessageId> getDispatchMessageIds() {
		return dispatchMessageIds;
	}

	public void setDispatchMessageIds(List<DispatchMessageId> dispatchMessageIds) {
		this.dispatchMessageIds = dispatchMessageIds;
	}

	public String getPushId() {
		return pushId;
	}

	public void setPushId(String pushId) {
		this.pushId = pushId;
	}

	public byte getToDeviceTypes() {
		return toDeviceTypes;
	}

	public void setToDeviceTypes(byte toDeviceTypes) {
		this.toDeviceTypes = toDeviceTypes;
	}

	/**
	 * 记录拆分消息的id信息
	 */
	public static class DispatchMessageId {
		private long toUid;

		private long msgId;

		private String msgUuid;

		public DispatchMessageId() {
		}

		public DispatchMessageId(long toUid, long msgId, String msgUuid) {
			this.toUid = toUid;
			this.msgId = msgId;
			this.msgUuid = msgUuid;
		}

		public long getToUid() {
			return toUid;
		}

		public void setToUid(long toUid) {
			this.toUid = toUid;
		}

		public long getMsgId() {
			return msgId;
		}

		public void setMsgId(long msgId) {
			this.msgId = msgId;
		}

		public String getMsgUuid() {
			return msgUuid;
		}

		public void setMsgUuid(String msgUuid) {
			this.msgUuid = msgUuid;
		}

		public String toString() {
			return "DispatchMessageId{ toUid=" + toUid + ", msgId=" + msgId + ", msgUuid=" + msgUuid + "}";
		}
	}

}
