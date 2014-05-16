package com.dianping.cat.report.page.userMonitor.graph;

import java.util.Date;
import java.util.Map;

import com.alibaba.cobar.parser.util.Pair;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PieChart;

public interface UserMonitorGraphCreator {

	public Map<String, LineChart> queryBaseInfo(Date start, Date end, String url, Map<String, String> pars);

	public Pair<LineChart, PieChart> queryErrorInfo(Date startDate, Date endDate, String url, Map<String, String> pars);

}
