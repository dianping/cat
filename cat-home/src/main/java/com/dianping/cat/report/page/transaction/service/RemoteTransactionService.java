package com.dianping.cat.report.page.transaction.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteTransactionService extends BaseRemoteModelService<TransactionReport> {
	public RemoteTransactionService() {
		super(TransactionAnalyzer.ID);
	}

	@Override
	protected TransactionReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
