package com.dianping.dog.alarm.connector;

import java.util.Date;

import com.dianping.dog.alarm.entity.ConnectEntity;
import com.dianping.dog.alarm.parser.DataParser;
import com.dianping.dog.alarm.parser.DataParserFactory;

public abstract class AbstractConnector<T> implements Connector {

	private long connectorId;

	private ConnectEntity m_entity;

	private DataParserFactory m_parserFactory;
	
	private RowData lastRowData;

	@Override
	public void init(ConnectEntity entity,DataParserFactory parserFactory) {
		this.m_entity = entity;
		this.m_parserFactory = parserFactory;
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

	public final RowData produceData(Date currentTime) {
		T content = fetchContent(m_entity);
		DataParser parser = m_parserFactory.getDataParser(m_entity.getUrl());
		RowData rowData = parser.parse(content);
		rowData.addData("domain",m_entity.getDomain());
		rowData.addData("type",m_entity.getType());
		rowData.addData("report",m_entity.getReport());
		if(lastRowData == null){
			lastRowData = rowData;
			return null;
		}
		
		return rowData;
	}
	
	public void setParserFactory(DataParserFactory factory) {
		this.m_parserFactory = factory;
	}

}
