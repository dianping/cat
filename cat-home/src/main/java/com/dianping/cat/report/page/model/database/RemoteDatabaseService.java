package com.dianping.cat.report.page.model.database;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteDatabaseService extends BaseRemoteModelService<DatabaseReport> {
	public RemoteDatabaseService() {
		super("database");
	}

	@Override
	protected DatabaseReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
