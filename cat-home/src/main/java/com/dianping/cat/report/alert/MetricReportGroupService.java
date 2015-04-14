package com.dianping.cat.report.alert;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.alert.MetricReportGroup.State;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class MetricReportGroupService {

	@Inject
	private ModelService<MetricReport> m_service;

	public MetricReport fetchMetricReport(String product, ModelPeriod period) {
		ModelRequest request = new ModelRequest(product, period.getStartTime()).setProperty("requireAll", "ture");
		ModelResponse<MetricReport> response = m_service.invoke(request);

		if (response != null) {
			return response.getModel();
		} else {
			return null;
		}
	}

	public MetricReport fetchMetricReport(ModelRequest request) {
		request.setProperty("requireAll", "ture");

		ModelResponse<MetricReport> response = m_service.invoke(request);

		if (response != null) {
			return response.getModel();
		} else {
			return null;
		}
	}

	public MetricReportGroup prepareDatas(String product, int minute, int duration) {
		MetricReport currentReport = null;
		MetricReport lastReport = null;
		boolean dataReady = false;
		State type = null;

		if (minute >= duration - 1) {
			type = State.CURRENT;
			currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);

			if (currentReport != null) {
				dataReady = true;
			}
		} else if (minute < 0) {
			type = State.LAST;
			lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null) {
				dataReady = true;
			}
		} else {
			type = State.CURRENT_LAST;
			currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);
			lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null && currentReport != null) {
				dataReady = true;
			}
		}
		MetricReportGroup reports = new MetricReportGroup();

		reports.setType(type).setLast(lastReport).setCurrent(currentReport).setDataReady(dataReady);
		return reports;
	}

}
