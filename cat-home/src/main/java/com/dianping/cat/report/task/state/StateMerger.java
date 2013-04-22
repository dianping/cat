package com.dianping.cat.report.task.state;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.core.dal.Report;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.state.StateReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportMerger;

public class StateMerger implements ReportMerger<StateReport> {

	@Override
	public StateReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		StateReportMerger merger = new StateReportMerger(new StateReport(reportDomain));
		for (Report report : reports) {
			String xml = report.getContent();
			StateReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		StateReport stateReport = merger.getStateReport();
		Date date = stateReport.getStartTime();
		stateReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		stateReport.setEndTime(end);
		
		for(Machine machine:stateReport.getMachines().values()){
			machine.getMessages().clear();
		}
		return stateReport;
	}

	@Override
	public StateReport mergeForGraph(String reportDomain, List<Report> reports) {
		throw new RuntimeException("State report don't need graph!");
	}
}
