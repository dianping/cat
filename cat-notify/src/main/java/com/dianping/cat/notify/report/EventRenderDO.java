package com.dianping.cat.notify.report;

public class EventRenderDO {
	private String id;

	private String totalCount;

	private String failCount;

	private String failPercent;

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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

}
