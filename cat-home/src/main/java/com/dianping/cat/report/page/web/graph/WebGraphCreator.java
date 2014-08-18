package com.dianping.cat.report.page.web.graph;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.tuple.Pair;

import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PieChart;

public interface WebGraphCreator {

	public Pair<Map<String, LineChart>,List<PieChart>> queryBaseInfo(Date start, Date end, String url, Map<String, String> pars);

	public Pair<LineChart, PieChart> queryErrorInfo(Date startDate, Date endDate, String url, Map<String, String> pars);

}
