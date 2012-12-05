package com.dianping.cat.report.page.model.event;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteEventService extends BaseRemoteModelService<EventReport> {
	public RemoteEventService() {
		super("event");
	}

	@Override
	protected EventReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
