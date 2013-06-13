package com.dianping.cat.report.page.model.heartbeat;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteHeartbeatService extends BaseRemoteModelService<HeartbeatReport> {
	public RemoteHeartbeatService() {
		super("heartbeat");
	}

	@Override
	protected HeartbeatReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
