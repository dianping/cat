package com.dianping.dog.alarm.connector;

import java.util.Calendar;
import java.util.Date;

import com.dianping.dog.alarm.entity.ConnectEntity;
import com.dianping.dog.alarm.parser.DataParser;
import com.dianping.dog.alarm.parser.DataParserFactory;

public abstract class AbstractConnector<T> implements Connector {

	private ConnectEntity m_entity;

	private DataParserFactory m_parserFactory;

	private RowData lastRowData;

	@Override
	public void init(ConnectEntity entity, DataParserFactory parserFactory) {
		this.m_entity = entity;
		this.m_parserFactory = parserFactory;
	}

	@Override
	public ConnectEntity getConnectorEntity() {
		return m_entity;
	}

	@Override
	public int getConnectorId() {
		return m_entity.getConId();
	}

	public abstract T fetchContent(ConnectEntity m_entity);

	public final RowData produceData(Date currentTime) {
		T content = fetchContent(m_entity);
		DataParser parser = m_parserFactory.getDataParser(m_entity);
		RowData rowData = parser.parse(m_entity, content);
		if (rowData == null) {
			return null;
		}
		if (lastRowData == null) {
			lastRowData = rowData;
			return null;
		}
		int hourSpan = compare(lastRowData.getTimeStamp());
		if (hourSpan < 0) {
			lastRowData = rowData;
			return rowData;
		} else if (hourSpan == 0) {
			RowData finalRowData = parser.mergeRowData(rowData, lastRowData);
			lastRowData = rowData;
			return finalRowData;
		} else {
			return null;
		}
	}

	public int compare(long timestamp) {
		long currentTime = System.currentTimeMillis();
		int curHour = getHourOfDay(currentTime);
		int paramHour = getHourOfDay(timestamp);
		return paramHour - curHour;
	}

	public int getHourOfDay(long timestamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(timestamp));
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	public void setParserFactory(DataParserFactory factory) {
		this.m_parserFactory = factory;
	}

}
