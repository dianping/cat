package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.system.config.AlertConfigManager;

public abstract class ProductlineContactor extends DefaultContactor implements Contactor {

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected AlertConfigManager m_configManager;

	@Override
	public List<String> queryEmailContactors(String id) {
		ProductLine productline = queryProductline(id);
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			mailReceivers.addAll(split(productline.getEmail()));

			return mailReceivers;
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		ProductLine productline = queryProductline(id);
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));
			smsReceivers.addAll(split(productline.getPhone()));

			return smsReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		ProductLine productline = queryProductline(id);
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));
			weixinReceivers.addAll(split(productline.getEmail()));

			return weixinReceivers;
		}
	}

	private ProductLine queryProductline(String productlineName) {
		try {
			ProductLine productline = m_productLineConfigManager.queryProductLine(productlineName);
			return productline;
		} catch (Exception e) {
			Cat.logError("query productline error:" + productlineName, e);
			return new ProductLine();
		}
	}
}
