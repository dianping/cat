package com.dianping.cat.report.page.metric.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteMetricService extends BaseRemoteModelService<MetricReport> {
	public RemoteMetricService() {
		super(MetricAnalyzer.ID);
	}

	@Override
	protected MetricReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
