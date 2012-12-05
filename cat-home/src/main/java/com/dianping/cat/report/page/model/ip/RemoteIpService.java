package com.dianping.cat.report.page.model.ip;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteIpService extends BaseRemoteModelService<IpReport> {
	public RemoteIpService() {
		super("ip");
	}

	@Override
	protected IpReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
