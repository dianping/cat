package com.dianping.cat.alarm.server.network;

import java.util.Collections;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.receiver.entity.Receiver;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.DefaultContactor;

public class ServerNetworkContactor extends DefaultContactor implements Contactor {

	public static final String ID = AlertType.SERVER_NETWORK.getName();

	@Inject
	protected AlertConfigManager m_configManager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
   public List<String> queryDXContactors(String id) {
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && receiver.isEnable()) {
			return buildDefaultDXReceivers(receiver);
		} else {
			return Collections.emptyList();
		}
   }

	@Override
	public List<String> queryEmailContactors(String id) {
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && receiver.isEnable()) {
			return buildDefaultMailReceivers(receiver);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && receiver.isEnable()) {
			return buildDefaultSMSReceivers(receiver);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && receiver.isEnable()) {
			return buildDefaultWeixinReceivers(receiver);
		} else {
			return Collections.emptyList();
		}
	}

}
