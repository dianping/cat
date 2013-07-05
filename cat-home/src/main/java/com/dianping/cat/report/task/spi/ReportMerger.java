/**
 * 
 */
package com.dianping.cat.report.task.spi;

import java.util.List;
import java.util.Set;

import com.dianping.cat.core.dal.HourlyReport;

public interface ReportMerger<E> {

	public E mergeForDaily(String reportDomain, List<HourlyReport> reports, Set<String> domains);

	public E mergeForGraph(String reportDomain, List<HourlyReport> reports);
}
