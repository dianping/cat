package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.aggregation.AggregationConfigManager;
import com.dianping.cat.configuration.aggreation.model.entity.AggregationRule;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.system.config.AlertConfigManager;

public class FrontEndExceptionContactor extends DefaultContactor implements Contactor {

	@Inject
	private AggregationConfigManager m_aggConfigManager;

	@Inject
	protected AlertConfigManager m_alertConfigManager;

	public static final String ID = AlertType.FRONT_END_EXCEPTION;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		List<String> mailReceivers = new ArrayList<String>();

		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			
			AggregationRule rule = m_aggConfigManager.queryAggration(id);
			if (rule != null) {
				mailReceivers.addAll(split(rule.getMails()));
			}
			return mailReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		return null;
	}

	@Override
	public List<String> querySmsContactors(String id) {
		return null;
	}
}
