package com.dianping.cat.report.page.userMonitor.graph;

import java.util.Date;

public interface UserMonitorCreator {

	public void queryBaseInfo(Date start, Date end, String url, String city, String channel);

	public void queryHttpCodeInfo(Date start,Date end,String url, String city, String channel);

	public void queryErrorCodeInfo(Date start,Date end,String url, String city, String channel);

}
