package com.dianping.cat.report.page.cross.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteCrossService extends BaseRemoteModelService<CrossReport> {
	public RemoteCrossService() {
		super(CrossAnalyzer.ID);
	}

	@Override
	protected CrossReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
