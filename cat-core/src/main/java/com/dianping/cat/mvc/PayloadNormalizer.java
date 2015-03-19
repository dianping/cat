package com.dianping.cat.mvc;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.Action;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.service.ModelPeriod;

public class PayloadNormalizer {

	@Inject
	private ServerConfigManager m_manager;

	public void normalize(ReportModel model, ReportPayload payload) {
		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}
		if (StringUtils.isEmpty(payload.getIpAddress())) {
			payload.setIpAddress(Constants.ALL);
		}
		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}
		model.setIpAddress(payload.getIpAddress());
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

	public interface ReportModel {
		public void setLongDate(long date);

		public void setIpAddress(String ip);

		public void setDisplayDomain(String domian);

		public void setCustomDate(Date start, Date end);

		public void setReportType(String reportType);
	}

	public interface ReportPayload {
		public String getDomain();

		public void setDomain(String domain);

		public String getIpAddress();

		public void setIpAddress(String ip);

		public ModelPeriod getPeriod();

		public long getCurrentDate();

		public long getDate();

		public String getReportType();

		public void setReportType(String reportType);

		public void computeStartDate();

		public void setYesterdayDefault();

		public boolean isToday();

		public Date getHistoryStartDate();

		public Date getHistoryEndDate();

		public Action getAction();
	}
	
}
