package com.dianping.cat.report.task.alert;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.system.config.AlertConfigManager;
import com.site.helper.Splitters;

public abstract class BaseAlertConfig {

	@Inject
	protected AlertConfigManager m_manager;

	protected List<String> buildDefaultMailReceivers(Receiver receiver) {
		List<String> mailReceivers = new ArrayList<String>();

		if (receiver != null) {
			mailReceivers.addAll(receiver.getEmails());
		}
		return mailReceivers;
	}

	private List<String> buildDefaultSMSReceivers(Receiver receiver) {
		List<String> smsReceivers = new ArrayList<String>();

		if (receiver != null) {
			smsReceivers.addAll(receiver.getPhones());
		}
		return smsReceivers;
	}

	public List<String> buildMailReceivers(ProductLine productLine) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_manager.getReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			mailReceivers.addAll(buildProductlineMailReceivers(productLine));

			return mailReceivers;
		}
	}

	private List<String> buildProductlineMailReceivers(ProductLine productLine) {
		return split(productLine.getEmail());
	}

	private List<String> buildProductlineSMSReceivers(ProductLine productLine) {
		return split(productLine.getPhone());
	}

	public List<String> buildSMSReceivers(ProductLine productLine) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_manager.getReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));
			smsReceivers.addAll(buildProductlineSMSReceivers(productLine));

			return smsReceivers;
		}
	}

	public abstract String getId();

	protected List<String> split(String str) {
		List<String> result = new ArrayList<String>();

		if (str != null) {
			result.addAll(Splitters.by(",").noEmptyItem().split(str));
		}

		return result;
	}
}
