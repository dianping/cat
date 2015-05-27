package com.dianping.cat.heartbeat.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.heartbeat.analyzer.HeartbeatAnalyzer;
import com.dianping.cat.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteHeartbeatService extends BaseRemoteModelService<HeartbeatReport> {
	public RemoteHeartbeatService() {
		super(HeartbeatAnalyzer.ID);
	}

	@Override
	protected HeartbeatReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
