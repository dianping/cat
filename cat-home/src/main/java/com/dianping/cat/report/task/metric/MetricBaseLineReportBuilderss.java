package com.dianping.cat.report.task.metric;
//package com.dianping.cat.report.task.metric;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import org.unidal.dal.jdbc.DalException;
//import org.unidal.lookup.annotation.Inject;
//
//import com.dianping.cat.Cat;
//import com.dianping.cat.consumer.metric.model.entity.MetricReport;
//import com.dianping.cat.home.dal.report.Dailyreport;
//import com.dianping.cat.report.service.HourlyReportService;
//import com.dianping.cat.report.task.TaskHelper;
//import com.dianping.cat.report.task.spi.AbstractReportBuilder;
//import com.dianping.cat.report.task.spi.ReportBuilder;
//
//public class MetricBaseLineReportBuilder extends AbstractReportBuilder implements ReportBuilder {
//	
//	@Inject
//	protected HourlyReportService m_hourlyReportService;
//	
//	@Inject
//	protected BussinessConfigManager m_configManager;
//	
//	private double[] m_weights = {40,30,20,10,10};
//	
//	private int[] m_related_dates = {7,14,21,1,2};
//	
//	//TODO
//	private void loadConfig(){
//		
//	}
//
//	@Override
//	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
//		loadConfig();
//		try {
//			Dailyreport report = gethourlyReport(reportName, reportDomain, reportPeriod);
//			m_dailyReportDao.insert(report);
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			Cat.logError(e);
//			return false;
//		}	
//	}
//
//	@Override
//	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod) {
//		
//		throw new RuntimeException("Metric base line report don't support hourly report!");
//	}
//
//	private Dailyreport gethourlyReport(String reportName, String group, Date reportPeriod) throws DalException {
//		List<MetricReport> reports = new ArrayList<MetricReport>();
//		List<Date> dates = getRelatedDateList(reportPeriod);
//		for(Date date:dates){
//			MetricReport report = m_hourlyReportService.queryMetricReport(group, date, date);
//			reports.add(report);
//		}
//		MetricReport baseLine = calculateBaseLine(reports,group);
//		baseLine.setStartTime(reportPeriod);
//		baseLine.setEndTime(TaskHelper.tomorrowZero(reportPeriod));
//		Dailyreport report = m_dailyReportDao.createLocal();
//		report.setContent(baseLine.toString());
//		report.setCreationDate(new Date());
//		report.setName(reportName);
//		report.setPeriod(reportPeriod);
//		report.setType(2);
//		return report;
//
//	}
//	
//	private MetricReport calculateBaseLine(List<MetricReport> reports,String group){
//		double totalWeight = 0;
//		int index = 0;
//		MetricReportMerger merger = new MetricReportMerger(new MetricReport(group));
//
//		for(MetricReport report:reports){
//			if(index >= m_weights.length) {
//				break;
//			}
//			double weight = m_weights[index];
//			if( weight == 0 ) {
//				index ++;
//				continue;
//			}
//			merger.setWeight(totalWeight/m_weights[index]);
//			report.accept(merger);
//			totalWeight += m_weights[index];
//			index ++;
//			
//		}
//		MetricReport baseLine = merger.getMetricReport();
//		return baseLine;
//		
//	}
//	
//	private List<Date> getRelatedDateList(Date reportPeriod) {
//		List<Date> dateList = new ArrayList<Date>();
//		for(int num:m_related_dates) {
//			dateList.add(TaskHelper.nDayAgo(reportPeriod, num));
//		}
//		return dateList;
//	}
//
//	@Override
//	public boolean buildMonthReport(String reportName, String reportDomain, Date reportPeriod) {
//		throw new RuntimeException("Metric base line report don't support monthly report!");
//	}
//
//	@Override
//	public boolean buildWeeklyReport(String reportName, String reportDomain, Date reportPeriod) {
//		throw new RuntimeException("Metric base line report don't support weekly report!");
//	}
//
//}
