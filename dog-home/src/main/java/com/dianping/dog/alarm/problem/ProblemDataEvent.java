package com.dianping.dog.alarm.problem;

import com.dianping.dog.alarm.connector.RowData;
import com.dianping.dog.alarm.data.DataEvent;
import com.dianping.dog.event.EventType;

public class ProblemDataEvent implements DataEvent {
	
   private RowData rowData;

	public ProblemDataEvent(RowData rowData){
	   this.rowData = rowData;
   }

	@Override
	public long getTimestamp() {
		return rowData.getTimeStamp();
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
		return EventType.ProblemDataEvent;
	}
	
	/*public String getUnicodeString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.getIp() + "@");
		sb.append(this.getReport()+"@");
		sb.append(this.getDomain() + "@");
		sb.append(this.getType() + "@");
		sb.append(this.getName());
		return sb.toString();
	}*/

	@Override
   public int getDataId() {
	   return rowData.getId();
   }

}
