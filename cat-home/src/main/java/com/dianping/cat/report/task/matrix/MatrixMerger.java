package com.dianping.cat.report.task.matrix;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.core.dal.Report;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.advanced.MatrixReportFilter;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.matrix.MatrixReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportMerger;

public class MatrixMerger implements ReportMerger<MatrixReport> {

	@Override
	public MatrixReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(reportDomain));
		for (Report report : reports) {
			String xml = report.getContent();
			MatrixReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		MatrixReport matrixReport = merger.getMatrixReport();
		Date date = matrixReport.getStartTime();
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);

		matrixReport.getDomainNames().addAll(domains);
		matrixReport.setStartTime(TaskHelper.todayZero(date));
		matrixReport.setEndTime(end);

		new MatrixReportFilter().visitMatrixReport(matrixReport);
		return matrixReport;
	}

	@Override
	public MatrixReport mergeForGraph(String reportDomain, List<Report> reports) {
		throw new RuntimeException("Matrix report don't need graph!");
	}
}
