package com.dianping.cat.alarm.spi.receiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.alarm.spi.AlertChannel;

@Named
public class ContactorManager extends ContainerHolder implements Initializable {

	private Map<String, Contactor> m_contactors = new HashMap<String, Contactor>();

	@Override
	public void initialize() throws InitializationException {
		m_contactors = lookupMap(Contactor.class);
	}

	public List<String> queryReceivers(String group, AlertChannel channel, String type) {
		Contactor contactor = m_contactors.get(type);

		if (AlertChannel.MAIL == channel) {
			return contactor.queryEmailContactors(group);
		} else if (AlertChannel.SMS == channel) {
			return contactor.querySmsContactors(group);
		} else if (AlertChannel.WEIXIN == channel) {
			return contactor.queryWeiXinContactors(group);
		} else if (AlertChannel.DX == channel) {
			return contactor.queryDXContactors(group);
		} else {
			throw new RuntimeException("unsupported channnel");
		}
	}

}
