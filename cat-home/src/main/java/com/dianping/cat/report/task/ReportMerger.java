/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.List;

import com.dianping.cat.hadoop.dal.Report;

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public interface ReportMerger<E> {

	public E merge(String reportDomain, List<Report> reports);
	
	public String mergeAll(String reportDomain, List<Report> reports);
}
