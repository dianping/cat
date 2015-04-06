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

	private MetricReport fetchMetricReport(String product, ModelPeriod period, int min, int max) {
		ModelRequest request = new ModelRequest(product, period.getStartTime()).setProperty("requireAll", "ture");

		request.setProperty("min", String.valueOf(min));
		request.setProperty("max", String.valueOf(max));

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
			int min = minute - duration + 1;
			int max = minute;

			type = State.CURRENT;
			currentReport = fetchMetricReport(product, ModelPeriod.CURRENT, min, max);

			if (currentReport != null) {
				dataReady = true;
			}
		} else if (minute < 0) {
			int min = minute + 60 - duration + 1;
			int max = minute + 60;

			type = State.LAST;
			lastReport = fetchMetricReport(product, ModelPeriod.LAST, min, max);

			if (lastReport != null) {
				dataReady = true;
			}
		} else {
			int lastLength = duration - minute - 1;
			int lastMin = 60 - lastLength;
			
			type = State.CURRENT_LAST;
			currentReport = fetchMetricReport(product, ModelPeriod.CURRENT, 0, minute);
			lastReport = fetchMetricReport(product, ModelPeriod.LAST, lastMin, 59);

			if (lastReport != null && currentReport != null) {
				dataReady = true;
			}
		}
		MetricReportGroup reports = new MetricReportGroup();

		reports.setType(type).setLast(lastReport).setCurrent(currentReport).setDataReady(dataReady);
		return reports;
	}

}
