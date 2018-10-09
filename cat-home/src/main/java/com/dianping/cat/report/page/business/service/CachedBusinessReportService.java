package com.dianping.cat.report.page.business.service;

import java.util.Date;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class CachedBusinessReportService {

	@Inject
	private BusinessReportService m_reportService;

	@Inject(type = ModelService.class, value = BusinessAnalyzer.ID)
	private ModelService<BusinessReport> m_service;

	private final Map<String, BusinessReport> m_businessReports = new LinkedHashMap<String, BusinessReport>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, BusinessReport> eldest) {
			return size() > 1000;
		}
	};

	public BusinessReport queryBusinessReport(String domain, Date start) {
		long time = start.getTime();
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(domain, time);

			if (m_service.isEligable(request)) {
				ModelResponse<BusinessReport> response = m_service.invoke(request);
				BusinessReport report = response.getModel();

				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable business service registered for " + request + "!");
			}
		} else {
			return getReportFromCache(domain, time);
		}
	}

	private BusinessReport getReportFromCache(String domain, long time) {
		String key = domain + time;
		BusinessReport result = m_businessReports.get(key);

		if (result == null) {
			Date start = new Date(time);
			Date end = new Date(time + TimeHelper.ONE_HOUR);

			result = m_reportService.queryReport(domain, start, end);
			m_businessReports.put(key, result);
		}
		return result;
	}
}
