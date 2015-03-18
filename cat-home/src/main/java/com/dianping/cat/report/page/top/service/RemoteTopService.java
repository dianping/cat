package com.dianping.cat.report.page.top.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteTopService extends BaseRemoteModelService<TopReport> {
	public RemoteTopService() {
		super(TopAnalyzer.ID);
	}

	@Override
	protected TopReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
