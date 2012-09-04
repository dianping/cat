package com.dianping.dog.connector;

import com.dianping.dog.event.DataEvent;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.parser.DataParser;
import com.dianping.dog.parser.DataParserFactory;

public abstract class AbstractConnector<T> implements Connector {

	private long connectorId;

	private ConnectorContext m_ctx;
	
   private DataParserFactory m_parserFactory;
   
	private EventDispatcher m_dispatcher;

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

	public boolean produceData(){
		T content = fetchContent(m_ctx);
		DataParser parser = m_parserFactory.getDataParser(m_ctx.getUrl());
      DataEvent event = new DataEvent();
      event.setRowData(parser.parse(content));
      m_dispatcher.dispatch(event);
		return true;
	}

	public void setDispatcher(EventDispatcher dispatcher) {
		m_dispatcher = dispatcher;
	}

}
