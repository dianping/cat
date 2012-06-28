/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.Date;
import java.util.List;

import com.dianping.cat.hadoop.dal.Graph;

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public interface GraphCreator<E> {

	List<Graph> splitReportToGraphs(Date reportPeriod, String reportDomain, String reportName, E report);
}
