/**
 * 
 */
package com.dianping.cat.report.task.spi;

import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.core.dal.Report;

public interface ReportMerger<E> {

	public E mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains);

	public E mergeForGraph(String reportDomain, List<Report> reports);
}
