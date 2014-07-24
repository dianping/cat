package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.system.config.AlertConfigManager;
import com.site.helper.Splitters;

public abstract class Seeker {

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected AlertConfigManager m_configManager;

	public List<String> queryReceivers(String productlineName, AlertChannel channel) {
		ProductLine productline = m_productLineConfigManager.queryProductLine(productlineName);

		if (channel == AlertChannel.MAIL) {
			return buildMailReceivers(productline);
		}

		if (channel == AlertChannel.WEIXIN) {
			return buildWeixinReceivers(productline);
		}

		if (channel == AlertChannel.SMS) {
			return buildSMSReceivers(productline);
		}

		return new ArrayList<String>();
	}

	private List<String> buildMailReceivers(ProductLine productLine) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getID());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			mailReceivers.addAll(buildProductlineMailReceivers(productLine));

			return mailReceivers;
		}
	}

	private List<String> buildDefaultMailReceivers(Receiver receiver) {
		List<String> mailReceivers = new ArrayList<String>();

		if (receiver != null) {
			mailReceivers.addAll(receiver.getEmails());
		}
		return mailReceivers;
	}

	private List<String> buildProductlineMailReceivers(ProductLine productLine) {
		return split(productLine.getEmail());
	}

	private List<String> buildSMSReceivers(ProductLine productLine) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getID());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));
			smsReceivers.addAll(buildProductlineSMSReceivers(productLine));

			return smsReceivers;
		}
	}

	private List<String> buildDefaultSMSReceivers(Receiver receiver) {
		List<String> smsReceivers = new ArrayList<String>();

		if (receiver != null) {
			smsReceivers.addAll(receiver.getPhones());
		}
		return smsReceivers;
	}

	private List<String> buildProductlineSMSReceivers(ProductLine productLine) {
		return split(productLine.getPhone());
	}

	private List<String> buildWeixinReceivers(ProductLine productLine) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getID());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));
			weixinReceivers.addAll(buildProductlineWeixinReceivers(productLine));

			return weixinReceivers;
		}
	}

	private List<String> buildDefaultWeixinReceivers(Receiver receiver) {
		List<String> weixinReceivers = new ArrayList<String>();

		if (receiver != null) {
			weixinReceivers.addAll(receiver.getWeixins());
		}
		return weixinReceivers;
	}

	private List<String> buildProductlineWeixinReceivers(ProductLine productLine) {
		return split(productLine.getEmail());
	}

	protected abstract String getID();

	private List<String> split(String str) {
		List<String> result = new ArrayList<String>();

		if (str != null) {
			result.addAll(Splitters.by(",").noEmptyItem().split(str));
		}

		return result;
	}

}
