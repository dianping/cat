/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.Date;
import java.util.List;

import com.dianping.cat.hadoop.dal.Graph;

public interface GraphCreator<E> {

	public List<Graph> splitReportToGraphs(Date reportPeriod, String reportDomain, String reportName, E report);
}
