package com.dianping.cat.report.page.model.sql;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteSqlService extends BaseRemoteModelService<SqlReport> {
	public RemoteSqlService() {
		super("sql");
	}

	@Override
	protected SqlReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
