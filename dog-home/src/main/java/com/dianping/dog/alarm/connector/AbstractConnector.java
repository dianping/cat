package com.dianping.dog.alarm.connector;

import java.util.Date;

import com.dianping.dog.alarm.parser.DataParser;
import com.dianping.dog.alarm.parser.DataParserFactory;

public abstract class AbstractConnector<T> implements Connector {

	private long connectorId;

	private ConnectorContext m_ctx;
	
   private DataParserFactory m_parserFactory;
   
	@Override
	public void init(ConnectorContext rule) {
		m_ctx = rule;
		this.connectorId = rule.getRuleId();
	}

	@Override
	public long getConnectorId() {
		return connectorId;
	}

	@Override
	public ConnectorContext getConnectorContext() {
		return m_ctx;
	}
	
	public abstract T fetchContent(ConnectorContext ctx);

	public final RowData produceData(Date currentTime){
		m_ctx.touch(currentTime);
		T content = fetchContent(m_ctx);
		DataParser parser = m_parserFactory.getDataParser(m_ctx.getUrl());
		return parser.parse(content);
	}

}
