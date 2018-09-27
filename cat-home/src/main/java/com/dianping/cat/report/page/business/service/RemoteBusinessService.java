package com.dianping.cat.report.page.business.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteBusinessService extends BaseRemoteModelService<BusinessReport> {

	public RemoteBusinessService() {
		super(BusinessAnalyzer.ID);
	}

	@Override
	protected BusinessReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}

	@Override
	public boolean isServersFixed() {
		return true;
	}

}
