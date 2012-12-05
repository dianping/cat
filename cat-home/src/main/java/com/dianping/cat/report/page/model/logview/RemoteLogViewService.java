package com.dianping.cat.report.page.model.logview;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteLogViewService extends BaseRemoteModelService<String> {
	public RemoteLogViewService() {
		super("logview");
	}

	@Override
	protected String buildModel(String content) throws SAXException, IOException {
		return content;
	}
}
