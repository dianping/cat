package com.dianping.cat.report.page.dependency.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteDependencyService extends BaseRemoteModelService<DependencyReport> {
	public RemoteDependencyService() {
		super(DependencyAnalyzer.ID);
	}

	@Override
	protected DependencyReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
