package com.dianping.dog.alarm.rule.store;

public class ExceptionData implements Data {
	
	private long timeStamp;
	
	private long totalCount;

	@Override
	public long getTimeStamp() {
		return timeStamp;
	}

	@Override
	public ExceptionData merge(Data data) {
		ExceptionData eData = (ExceptionData) data;
		this.timeStamp = data.getTimeStamp();
		this.totalCount += eData.getTotalCount();
		return this;
	}

	public long getTotalCount() {
   	return totalCount;
   }

	public void setTotalCount(long totalCount) {
   	this.totalCount = totalCount;
   }

	public void setTimeStamp(long timeStamp) {
   	this.timeStamp = timeStamp;
   }

}
