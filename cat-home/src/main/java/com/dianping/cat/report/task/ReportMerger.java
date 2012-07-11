/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.List;
import java.util.Set;

import com.dianping.cat.hadoop.dal.Report;

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public interface ReportMerger<E> {

	public E mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains);
	
	public E mergeForGraph(String reportDomain, List<Report> reports);
}
