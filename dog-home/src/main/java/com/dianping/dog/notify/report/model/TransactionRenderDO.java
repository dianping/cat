package com.dianping.dog.notify.report.model;

public class TransactionRenderDO {
	
	private String id;

	private String totalCount;

	private String failCount;

	private String failPercent;

	private String avg;

	private String tps;

	private String link;
	

	public String getId() {
   	return id;
   }

	public void setId(String id) {
   	this.id = id;
   }

	public String getTotalCount() {
   	return totalCount;
   }

	public void setTotalCount(String totalCount) {
   	this.totalCount = totalCount;
   }

	public String getFailCount() {
   	return failCount;
   }

	public void setFailCount(String failCount) {
   	this.failCount = failCount;
   }

	public String getFailPercent() {
   	return failPercent;
   }

	public void setFailPercent(String failPercent) {
   	this.failPercent = failPercent;
   }

	public String getAvg() {
   	return avg;
   }

	public void setAvg(String avg) {
   	this.avg = avg;
   }

	public String getTps() {
   	return tps;
   }

	public void setTps(String tps) {
   	this.tps = tps;
   }

	public String getLink() {
   	return link;
   }

	public void setLink(String link) {
   	this.link = link;
   }
	
}
