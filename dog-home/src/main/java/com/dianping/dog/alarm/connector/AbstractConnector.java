package com.dianping.dog.alarm.connector;

import java.util.Date;

import com.dianping.dog.alarm.entity.ConnectEntity;
import com.dianping.dog.alarm.parser.DataParser;
import com.dianping.dog.alarm.parser.DataParserFactory;

public abstract class AbstractConnector<T> implements Connector {

	private long connectorId;

	private ConnectEntity m_entity;
	
   private DataParserFactory m_parserFactory;
   
	@Override
	public void init(ConnectEntity entity) {
		m_entity = entity;
		this.connectorId = m_entity.getConId();
	}
	
	@Override
   public ConnectEntity getConnectorEntity() {
	   return m_entity;
   }

	@Override
	public long getConnectorId() {
		return connectorId;
	}

	public abstract T fetchContent(ConnectEntity m_entity);

	public final RowData produceData(Date currentTime){
		T content = fetchContent(m_entity);
		DataParser parser = m_parserFactory.getDataParser(m_entity.getUrl());
		return parser.parse(content);
	}

}
