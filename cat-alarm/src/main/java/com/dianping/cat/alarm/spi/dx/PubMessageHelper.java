package com.dianping.cat.alarm.spi.dx;

import com.dianping.cat.alarm.spi.dx.vo.BroadcastMessage;
import com.dianping.cat.alarm.spi.dx.vo.KFPushMessage;
import com.dianping.cat.alarm.spi.dx.vo.MessageType;
import com.dianping.cat.alarm.spi.dx.vo.PushMessage;
import com.dianping.cat.alarm.spi.dx.vo.XBody;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-5-10
 */
public class PubMessageHelper {

	public static PushMessage constructPushMessage(long fromUid, long[] toUids, short toAppId, long cts, byte[] packets) {
		PushMessage message = new PushMessage();
		message.setFromUid(fromUid);
		message.setToUids(toUids);
		message.setMessageType(MessageType.transmission.name());
		message.setToAppId(toAppId);
		message.setCts(cts);
		message.setBody(packets);
		return message;
	}

	public static PushMessage constructPushMessage(long fromUid, long[] toUids, String[] receivers, String fromName,
	      short toAppId, long cts, String extension, XBody body) {
		PushMessage message = new PushMessage();
		message.setFromUid(fromUid);
		message.setToUids(toUids);
		message.setReceivers(receivers);
		message.setFromName(fromName);
		message.setMessageType(body.messageType());
		message.setToAppId(toAppId);
		message.setBody(body);
		message.setCts(cts);
		if (extension != null) {
			message.setExtension(extension);
		}
		return message;
	}

	public static KFPushMessage constructKFPushMessage(long fromUid, long pubUid, long[] toUids, String[] receivers,
	      String fromName, short appId, short toAppId, long cts, String extension, XBody body) {
		KFPushMessage kfPushMessage = new KFPushMessage();
		kfPushMessage.setFromUid(fromUid);
		kfPushMessage.setToUids(toUids);
		kfPushMessage.setReceivers(receivers);
		kfPushMessage.setFromName(fromName);
		kfPushMessage.setMessageType(body.messageType());
		kfPushMessage.setAppId(appId);
		kfPushMessage.setToAppId(toAppId);
		kfPushMessage.setPubUid(pubUid);
		kfPushMessage.setBody(body);
		kfPushMessage.setCts(cts);
		if (extension != null) {
			kfPushMessage.setExtension(extension);
		}
		return kfPushMessage;
	}

	public static BroadcastMessage constructBroadcastMessage(long fromUid, short toAppId, long cts, String extension,
	      XBody body) {
		BroadcastMessage message = new BroadcastMessage();
		message.setFromUid(fromUid);
		message.setBody(body);
		message.setToAppId(toAppId);
		message.setMessageType(body.messageType());
		message.setCts(cts);
		if (extension != null) {
			message.setExtension(extension);
		}
		return message;
	}
}
