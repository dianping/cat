package com.dianping.dog.event;

import com.dianping.dog.connector.RowData;

public class DataEvent implements Event {
	
	private EventType m_type;
	
	private RowData m_rowData;

	public DataEvent() {
		m_type = DefaultEventType.DATA_EVENT;
	}

	@Override
	public EventType getType() {
		return m_type;
	}
	
	public void setRowData(RowData rowData){
		m_rowData  = rowData;
	}
	
	public RowData getRowData(){
		return m_rowData;
	}

}