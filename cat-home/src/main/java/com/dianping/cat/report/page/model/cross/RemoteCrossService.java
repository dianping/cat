package com.dianping.cat.report.page.model.cross;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteCrossService extends BaseRemoteModelService<CrossReport> {
	public RemoteCrossService() {
		super("cross");
	}

	@Override
	protected CrossReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
