package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.aggregation.AggregationConfigManager;
import com.dianping.cat.configuration.aggreation.model.entity.AggregationRule;
import com.dianping.cat.report.task.alert.AlertConstants;

public class FrontEndExceptionContactor implements Contactor {

	@Inject
	private AggregationConfigManager m_configManager;

	public static final String ID = AlertConstants.FRONT_END_EXCEPTION;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		AggregationRule rule = m_configManager.queryAggration(id);

		if (rule != null) {
			String mails = rule.getMails();
			List<String> receiver = Splitters.by(",").noEmptyItem().split(mails);

			return receiver;
		} else {
			return new ArrayList<String>();
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
