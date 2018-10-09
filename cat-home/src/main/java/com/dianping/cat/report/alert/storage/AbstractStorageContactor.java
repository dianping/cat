package com.dianping.cat.report.alert.storage;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.receiver.entity.Receiver;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.DefaultContactor;

public abstract class AbstractStorageContactor extends DefaultContactor implements Contactor {

	@Inject
	protected AlertConfigManager m_alertConfigManager;

	public abstract String getId();

	@Override
	public List<String> queryEmailContactors(String id) {
		List<String> mailReceivers = new ArrayList<String>();

		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));

			return mailReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));

			return weixinReceivers;
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));

			return smsReceivers;
		}
	}

	@Override
	public List<String> queryDXContactors(String id) {
		List<String> dxReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return dxReceivers;
		} else {
			dxReceivers.addAll(buildDefaultDXReceivers(receiver));

			return dxReceivers;
		}
	}

}
