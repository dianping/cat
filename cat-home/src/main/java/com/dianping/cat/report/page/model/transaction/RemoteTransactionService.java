package com.dianping.cat.report.page.model.transaction;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteTransactionService extends BaseRemoteModelService<TransactionReport> {
	public RemoteTransactionService() {
		super("transaction");
	}

	@Override
	protected TransactionReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
