package com.dianping.dog.connector;


public class RowData {
 
	private long startTime;

	private long endTime;
	
	private String domain;
	
	private String type;
	
	private String status;
	
	private long total;
	
	private long count;

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getDomain() {
   	return domain;
   }

	public void setDomain(String domain) {
   	this.domain = domain;
   }

	public String getType() {
   	return type;
   }

	public void setType(String type) {
   	this.type = type;
   }

	public String getStatus() {
   	return status;
   }

	public void setStatus(String status) {
   	this.status = status;
   }

	public long getTotal() {
   	return total;
   }

	public void setTotal(long total) {
   	this.total = total;
   }

	public long getCount() {
   	return count;
   }

	public void setCount(long count) {
   	this.count = count;
   }
	
}
