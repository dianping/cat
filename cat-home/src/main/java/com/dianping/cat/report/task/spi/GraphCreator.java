/**
 * 
 */
package com.dianping.cat.report.task.spi;

import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.core.dal.Graph;

public interface GraphCreator<E> {

	public List<Graph> splitReportToGraphs(Date reportPeriod, String reportDomain, String reportName, E report);
}
