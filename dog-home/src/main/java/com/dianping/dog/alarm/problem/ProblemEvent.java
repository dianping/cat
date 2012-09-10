package com.dianping.dog.alarm.problem;

import java.util.Date;

import com.dianping.dog.alarm.connector.RowData;
import com.dianping.dog.alarm.data.DataEvent;
import com.dianping.dog.event.EventType;

public class ProblemEvent implements DataEvent {
	
   private RowData rowData;

	public ProblemEvent(RowData rowData){
	   this.rowData = rowData;
   }

	@Override
	public Date getTimestamp() {
		return rowData.getData("time");
	}

	@Override
	public String getDomain() {
		return rowData.getData("domain");
	}

	@Override
	public String getIp() {
		return rowData.getData("ip");
	}

	@Override
	public String getReport() {
		return "problem";
	}

	@Override
	public String getType() {
		return rowData.getData("type");
	}

	@Override
	public String getName() {
		return rowData.getData("name");
	}

	@Override
	public long getTotalCount() {
		return (Long)rowData.getData("totalCount");
	}

	@Override
	public long getFailCount() {
		return (Long)rowData.getData("failCount");
	}

	@Override
	public EventType getEventType() {
		return EventType.ProblemEvent;
	}

}
