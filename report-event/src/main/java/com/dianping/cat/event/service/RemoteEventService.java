package com.dianping.cat.event.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.event.analyzer.EventAnalyzer;
import com.dianping.cat.event.model.entity.EventReport;
import com.dianping.cat.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteEventService extends BaseRemoteModelService<EventReport> {
	public RemoteEventService() {
		super(EventAnalyzer.ID);
	}

	@Override
	protected EventReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
