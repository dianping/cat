package com.dianping.cat.report.alert.business;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Named
public class BusinessReportGroupService {

	@Inject(BusinessAnalyzer.ID)
	private ModelService<BusinessReport> m_service;

	private BusinessReport fetchMetricReport(String product, ModelPeriod period, int min, int max) {
		ModelRequest request = new ModelRequest(product, period.getStartTime()).setProperty("requireAll", "ture");

		request.setProperty("min", String.valueOf(min));
		request.setProperty("max", String.valueOf(max));

		ModelResponse<BusinessReport> response = m_service.invoke(request);

		if (response != null) {
			return response.getModel();
		} else {
			return null;
		}
	}

	public BusinessReportGroup prepareDatas(String domain, int minute, int duration) {
		BusinessReport currentReport = null;
		BusinessReport lastReport = null;
		boolean dataReady = false;

		if (minute >= duration - 1) {
			int min = minute - duration + 1;
			int max = minute;

			currentReport = fetchMetricReport(domain, ModelPeriod.CURRENT, min, max);

			if (currentReport != null) {
				dataReady = true;
			}
		} else if (minute < 0) {
			int min = minute + 60 - duration + 1;
			int max = minute + 60;

			lastReport = fetchMetricReport(domain, ModelPeriod.LAST, min, max);

			if (lastReport != null) {
				dataReady = true;
			}
		} else {
			int lastLength = duration - minute - 1;
			int lastMin = 60 - lastLength;

			currentReport = fetchMetricReport(domain, ModelPeriod.CURRENT, 0, minute);
			lastReport = fetchMetricReport(domain, ModelPeriod.LAST, lastMin, 59);

			if (lastReport != null && currentReport != null) {
				dataReady = true;
			}
		}
		BusinessReportGroup reports = new BusinessReportGroup();

		reports.setLast(lastReport).setCurrent(currentReport).setDataReady(dataReady);
		return reports;
	}
}
