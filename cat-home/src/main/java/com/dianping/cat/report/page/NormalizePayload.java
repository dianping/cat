package com.dianping.cat.report.page;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.helper.CatString;

public class NormalizePayload {

	private ServerConfigManager m_manager;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void normalize(AbstractReportModel model, AbstractReportPayload payload) {
		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}
		if (StringUtils.isEmpty(payload.getIpAddress())) {
			payload.setIpAddress(CatString.ALL);
		}
		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}
		model.setIpAddress(payload.getIpAddress());
		model.setAction(payload.getAction());
		model.setDisplayDomain(payload.getDomain());

		if (payload.getAction().getName().startsWith("history")) {
			String type = payload.getReportType();
			if (type == null || type.length() == 0) {
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			if (!payload.isToday()) {
				payload.setYesterdayDefault();
			}
			model.setLongDate(payload.getDate());
			model.setCustomDate(payload.getHistoryStartDate(), payload.getHistoryEndDate());
		}
	}

}
