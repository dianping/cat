package com.dianping.cat.alarm.spi.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.helper.Splitters;

import com.dianping.cat.alarm.receiver.entity.Receiver;

public abstract class DefaultContactor {

	protected List<String> buildDefaultMailReceivers(Receiver receiver) {
		List<String> mailReceivers = new ArrayList<String>();

		if (receiver != null) {
			mailReceivers.addAll(receiver.getEmails());
		}
		return mailReceivers;
	}

	protected List<String> buildDefaultSMSReceivers(Receiver receiver) {
		List<String> smsReceivers = new ArrayList<String>();

		if (receiver != null) {
			smsReceivers.addAll(receiver.getPhones());
		}
		return smsReceivers;
	}

	protected List<String> buildDefaultDXReceivers(Receiver receiver) {
		List<String> receivers = new ArrayList<String>();

		if (receiver != null) {
			receivers.addAll(receiver.getDxs());
		}
		return receivers;
	}

	protected List<String> buildDefaultWeixinReceivers(Receiver receiver) {
		List<String> weixinReceivers = new ArrayList<String>();

		if (receiver != null) {
			weixinReceivers.addAll(receiver.getWeixins());
		}
		return weixinReceivers;
	}

	protected List<String> split(String str) {
		List<String> result = new ArrayList<String>();

		if (str != null) {
			result.addAll(Splitters.by(",").noEmptyItem().trim().split(str));
		}

		return result;
	}
}
