package com.dianping.cat.mvc;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.Action;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;

public class PayloadNormalizer {

	@Inject
	protected ServerConfigManager m_manager;

	public void normalize(ReportModel model, ReportPayload payload) {
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

	public interface ReportModel {
		public void setDate(long date);

		public void setIpAddress(String ip);

		public void setDisplayDomain(String domian);

		public void setCustomDate(Date start, Date end);

		public void setReportType(String reportType);
	}

	public interface ReportPayload {
		public String getDomain();

		public String getIpAddress();

		public long getDate();

		public String getReportType();

		public void computeHistoryDate();

		public Date getHistoryStartDate();

		public Date getHistoryEndDate();

		public Action getAction();
	}

}
