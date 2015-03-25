package com.dianping.cat.mvc;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;

public class PayloadNormalizer {

	@Inject
	protected ServerConfigManager m_manager;

	@SuppressWarnings("rawtypes")
   public void normalize(AbstractReportModel model, AbstractReportPayload payload) {
		long date = payload.getDate();
		long current = System.currentTimeMillis();

		if (date > current) {
			date = current - current % TimeHelper.ONE_HOUR;
			model.setDate(date);
		} else {
			model.setDate(date);
		}
		
		model.setIpAddress(payload.getIpAddress());
		model.setDisplayDomain(payload.getDomain());

		if (payload.getAction().getName().startsWith("history")) {
			payload.computeHistoryDate();

			Date start = payload.getHistoryStartDate();
			Date end = payload.getHistoryEndDate();
			
			model.setReportType(payload.getReportType());
			model.setDate(start.getTime());
			model.setCustomDate(start, end);
		}
	}

}
