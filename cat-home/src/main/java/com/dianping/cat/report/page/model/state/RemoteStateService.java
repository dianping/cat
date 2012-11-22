package com.dianping.cat.report.page.model.state;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteStateService extends BaseRemoteModelService<StateReport> {
	public RemoteStateService() {
		super("state");
	}

	@Override
	protected StateReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
