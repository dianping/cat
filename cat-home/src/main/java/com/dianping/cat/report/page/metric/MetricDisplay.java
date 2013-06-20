package com.dianping.cat.report.page.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.dianping.cat.consumer.advanced.BussinessConfigManager.BusinessConfig;
import com.dianping.cat.consumer.metric.model.entity.Abtest;
import com.dianping.cat.consumer.metric.model.entity.Group;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.LineChart;

public class MetricDisplay extends BaseVisitor {

	private Map<String, LineChart> m_lineCharts = new LinkedHashMap<String, LineChart>();

	private Set<String> m_abtests = new TreeSet<String>();

	private String m_abtest;

	private Date m_start;

	private String m_metricKey;

	private static final String SUM = ":sum";

	private static final String COUNT = ":count";

	private static final String AVG = "avg";
	
	public List<LineChart> getLineCharts(){
		return new ArrayList<LineChart>(m_lineCharts.values());
	}
	
	public Set<String> getAbtests(){
		return m_abtests;
	}

	public MetricDisplay(List<BusinessConfig> configs, String abtest, Date start) {
		m_start = start;
		m_abtest = abtest;

		for (BusinessConfig flag : configs) {
			if (flag.isShowSum()) {
				String key = flag.getMainKey() + SUM;
				
				m_lineCharts.put(key, creatLineChart(key));
			}
			if (flag.isShowCount()) {
				String key = flag.getMainKey() + COUNT;
				
				m_lineCharts.put(key, creatLineChart(key));

			}
			if (flag.isShowAvg()) {
				String key = flag.getMainKey() + AVG;
				
				m_lineCharts.put(key, creatLineChart(key));
			}
		}
	}

	private LineChart creatLineChart(String key) {
	   LineChart lineChart = new LineChart();
	   
	   lineChart.setTitles(key);
	   lineChart.setStart(m_start);
	   lineChart.setSize(60);
	   lineChart.setStep(TimeUtil.ONE_MINUTE);
	   return lineChart;
   }

	@Override
	public void visitAbtest(Abtest abtest) {
		String abtestId = abtest.getRunId();

		m_abtests.add(abtestId);
		if (m_abtest.equals(abtestId)) {
			super.visitAbtest(abtest);
		}
	}

	@Override
	public void visitGroup(Group group) {
		String id = group.getName();
		double[] sum = new double[60];
		double[] avg = new double[60];
		double[] count = new double[60];

		for (Point point : group.getPoints().values()) {
			int index = point.getId();

			sum[index] = point.getSum();
			avg[index] = point.getAvg();
			count[index] = point.getCount();
		}

		LineChart sumLine = m_lineCharts.get(m_metricKey + SUM);

		if (sumLine != null) {
			sumLine.addSubTitle(id);
			sumLine.addValue(sum);
		}
		LineChart countLine = m_lineCharts.get(m_metricKey + COUNT);

		if (countLine != null) {
			countLine.addSubTitle(id);
			countLine.addValue(count);
		}
		LineChart avgLine = m_lineCharts.get(m_metricKey + AVG);

		if (avgLine != null) {
			avgLine.addSubTitle(id);
			avgLine.addValue(avg);
		}
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		m_metricKey = metricItem.getId();
		super.visitMetricItem(metricItem);
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		super.visitMetricReport(metricReport);
	}

}
