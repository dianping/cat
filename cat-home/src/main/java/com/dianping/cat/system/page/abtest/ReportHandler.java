package com.dianping.cat.system.page.abtest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerLoader;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestEntity;
import com.dianping.cat.home.dal.abtest.AbtestReportDao;
import com.dianping.cat.home.dal.abtest.AbtestReportEntity;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.AbtestRunDao;
import com.dianping.cat.home.dal.abtest.AbtestRunEntity;
import com.dianping.cat.report.abtest.entity.AbtestReport;
import com.dianping.cat.report.abtest.entity.Chart;
import com.dianping.cat.report.abtest.entity.Goal;
import com.dianping.cat.report.abtest.entity.Variation;
import com.dianping.cat.report.abtest.transform.BaseVisitor;
import com.dianping.cat.report.abtest.transform.DefaultSaxParser;
import com.dianping.cat.report.task.abtest.ABTestReportBuilder;
import com.dianping.cat.system.page.abtest.ListViewModel.AbtestItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ReportHandler implements SubHandler, Initializable {
	@Inject
	private AbtestDao m_abtestDao;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	@Inject
	private AbtestReportDao m_abtestReportDao;

	@Inject
	private MetricConfigManager m_configManager;

	private static GsonBuilderManager m_gsonBuilderManager = new GsonBuilderManager();

	private Calendar m_calendar = Calendar.getInstance();

	private DateFormat m_dateFormatForHour = new SimpleDateFormat("MM-dd HH:00");

	private DateFormat m_dateFormatForDay = new SimpleDateFormat("MM-dd");

	private AbtestReport buildDailyReport(AbtestReport query, String goal, Model model) {
		Date startTime = query.getStartTime();
		Date endTime = query.getEndTime();
		m_calendar.setTime(startTime);

		long deltaTime = endTime.getTime() - startTime.getTime();
		long day = deltaTime / (24 * 60 * 60 * 1000L);
		int step = ((int) day / 7 > 0) ? ((int) day / 7) : 1;
		int count = 0;
		List<AbtestReport> datas = new ArrayList<AbtestReport>();
		List<String> labels = new ArrayList<String>();

		m_calendar.add(Calendar.DAY_OF_MONTH, 1);
		endTime = m_calendar.getTime();

		while (startTime.before(query.getEndTime())) {
			if (count % step == 0) {
				List<AbtestReport> reports = queryReport(query.getRunId(), startTime, endTime);

				AbtestReport report = mergeReport(reports);

				datas.add(report);

				labels.add(m_dateFormatForDay.format(endTime));
			} else {
				labels.add("");
			}

			count++;
			startTime = endTime;
			m_calendar.add(Calendar.DAY_OF_MONTH, 1);
			endTime = m_calendar.getTime();
		}

		AbtestReport report = mergeReport(datas);
		Chart chart = new Chart();

		if (goal.length() == 0) {
			for (Goal _goal : report.getGoals()) {
				goal = _goal.getName();

				if (goal.length() > 0) {
					break;
				}
			}
		}

		String datasets = buildDateSets(datas, goal, report.getVariations().keySet(), model);
		Gson gson = m_gsonBuilderManager.getGsonBuilder().create();

		String label = gson.toJson(labels, new TypeToken<List<String>>() {
		}.getType());

		chart.setType("day");
		chart.setLabels(label);
		chart.setDatasets(datasets);
		chart.setGoal(goal);

		report.setChart(chart);
		report.setRunId(query.getRunId());
		report.setStartTime(query.getStartTime());
		report.setEndTime(query.getEndTime());

		return report;
	}

	private String buildDateSets(List<AbtestReport> reports, String goal, Set<String> set, Model model) {
		List<DataSets> dataSets = new ArrayList<DataSets>();

		for (String key : set) {
			List<Number> data = new ArrayList<Number>();

			for (AbtestReport report : reports) {
				if (report.getStartTime() != null) {
					Variation variation = report.findVariation(key);

					if (variation != null) {
						Goal tmp = variation.findGoal(goal);

						if (tmp != null) {
							if (tmp.getType().equals("C")) {
								data.add(tmp.getCount());
							} else if (tmp.getType().equals("S")) {
								data.add(tmp.getSum());
							} else {
								data.add(tmp.getAvg());
							}
						} else {
							data.add(0);
						}
					} else {
						data.add(0);
					}
				} else {
					data.add(0);
				}
			}

			DataSets dataSet = DataSetsBuilder.buildDataSets(DataSetColor.getDataSetColor(key), data);
			dataSets.add(dataSet);
		}

		model.setDataSets(dataSets);
		Gson gson = m_gsonBuilderManager.getGsonBuilder().create();

		return gson.toJson(dataSets, new TypeToken<List<DataSets>>() {
		}.getType());
	}

	private AbtestReport buildHourlyReport(AbtestReport query, String goal, Model model) {
		Date startTime = query.getStartTime();
		Date endTime = query.getEndTime();

		List<AbtestReport> reports = queryReport(query.getRunId(), startTime, endTime);
		List<AbtestReport> datas = new ArrayList<AbtestReport>();
		List<String> labels = new ArrayList<String>();

		long deltaTime = endTime.getTime() - startTime.getTime();
		long hour = deltaTime / (60 * 60 * 1000L);
		int step = ((int) hour / 12 > 0) ? ((int) hour / 12) : 1;
		int size = reports.size();
		int count = 0;
		int i = 0;

		m_calendar.setTime(startTime);
		m_calendar.add(Calendar.HOUR, 1);
		endTime = m_calendar.getTime();

		while (startTime.before(query.getEndTime())) {
			if (count % step == 0) {
				labels.add(m_dateFormatForHour.format(startTime));
			} else {
				labels.add("");
			}

			if (i < size) {
				AbtestReport re = reports.get(i);

				if (re.getStartTime().equals(startTime)) {
					datas.add(re);

					i++;
				} else {
					datas.add(new AbtestReport());
				}
			}

			count++;
			startTime = endTime;
			m_calendar.add(Calendar.HOUR, 1);
			endTime = m_calendar.getTime();
		}

		AbtestReport report = mergeReport(datas);
		Chart chart = new Chart();

		if (goal.length() == 0) {
			for (Goal _goal : report.getGoals()) {
				goal = _goal.getName();

				if (goal.length() > 0) {
					break;
				}
			}
		}

		String datasets = buildDateSets(datas, goal, report.getVariations().keySet(), model);
		Gson gson = m_gsonBuilderManager.getGsonBuilder().create();

		String label = gson.toJson(labels, new TypeToken<List<String>>() {
		}.getType());

		chart.setType("hour");
		chart.setLabels(label);
		chart.setDatasets(datasets);
		chart.setGoal(goal);

		report.setChart(chart);
		report.setRunId(query.getRunId());
		report.setStartTime(query.getStartTime());
		report.setEndTime(query.getEndTime());

		return report;
	}

	private AbtestReport buildQuery(int runId, Date startTime, Date endTime, String period) {
		AbtestReport query = new AbtestReport();
		Date now = new Date();
		Date newStartTime = null;
		Date newEndTime = null;

		query.setRunId(runId);

		if (startTime == null && endTime == null) {
			m_calendar.setTime(now);

			if (period.equals("day")) {
				m_calendar.add(Calendar.DAY_OF_MONTH, -7);
			} else {
				m_calendar.add(Calendar.HOUR_OF_DAY, -24);
			}

			newStartTime = m_calendar.getTime();
			newEndTime = now;
		} else if (endTime == null) {
			m_calendar.setTime(startTime);

			if (period.equals("day")) {
				m_calendar.add(Calendar.DAY_OF_MONTH, 7);
			} else {
				m_calendar.add(Calendar.HOUR_OF_DAY, 24);
			}

			newStartTime = startTime;
			newEndTime = m_calendar.getTime();
		} else if (startTime == null) {
			m_calendar.setTime(endTime);

			if (period.equals("day")) {
				m_calendar.add(Calendar.DAY_OF_MONTH, -7);
			} else {
				m_calendar.add(Calendar.HOUR_OF_DAY, -24);
			}

			newStartTime = m_calendar.getTime();
			newEndTime = endTime;
		} else {
			newStartTime = startTime;
			newEndTime = endTime;
		}

		query.setStartTime(resetTime(period, newStartTime));
		query.setEndTime(resetTime(period, newEndTime));

		return query;
	}

	public AbtestReport buildReport(AbtestReport query, String goal, String period, Model model) {
		if (period.equals("day")) {
			return buildDailyReport(query, goal, model);
		} else {
			return buildHourlyReport(query, goal, model);
		}
	}

	public Map<String, MetricItemConfig> getMetricItemConfig() {
		return m_configManager.getMetricConfig().getMetricItemConfigs();
	}

	@Override
	public void handle(Context ctx, Model model, Payload payload) {
		int runId = payload.getId();
		try {
			AbtestRun run = m_abtestRunDao.findByPK(runId, AbtestRunEntity.READSET_FULL);
			Abtest abtest = m_abtestDao.findByPK(run.getCaseId(), AbtestEntity.READSET_FULL);

			AbtestItem item = new AbtestItem(abtest, run);

			Date startTime = payload.getStartDate();
			Date endTime = payload.getEndDate();
			String goal = payload.getSelectMetricType();
			String period = payload.getPeriod();

			if (period == null || (!period.equals("hour") && !period.equals("day"))) {
				period = "hour";
			}

			if (goal == null) {
				goal = "";
			}

			AbtestReport query = buildQuery(runId, startTime, endTime, period);
			AbtestReport report = buildReport(query, goal, period, model);

			Collections.sort(report.getGoals(), new Comparator<Goal>() {
				@Override
				public int compare(Goal o1, Goal o2) {
					Map<String, MetricItemConfig> metricItemConfig = getMetricItemConfig();

					MetricItemConfig item1 = metricItemConfig.get(o1.getName());
					MetricItemConfig item2 = metricItemConfig.get(o2.getName());

					if (item1.getViewOrder() > item2.getViewOrder()) {
						return 1;
					} else {
						return -1;
					}
				}
			});

			model.setAbtest(item);
			model.setReport(report);
			model.setMetricConfigItem(getMetricItemConfig());

			payload.setStartDate2(query.getStartTime());
			payload.setEndDate2(query.getEndTime());
		} catch (Exception e) {
			Cat.logError(e);
			e.printStackTrace();
		}
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			ContainerLoader.getDefaultContainer().lookup(ABTestReportBuilder.class);
		} catch (ComponentLookupException e) {
			Cat.logError(e);
		}
	}

	private AbtestReport mergeReport(List<AbtestReport> reports) {
		AbtestReport result = new AbtestReport();

		AbtestReportVisitor visitor = new AbtestReportVisitor(result);

		for (AbtestReport report : reports) {
			if (report.getStartTime() != null) {
				visitor.visitAbtestReport(report);

				result.setRunId(report.getRunId());

				if (result.getStartTime() == null || result.getEndTime() == null) {
					result.setStartTime(report.getStartTime());
					result.setEndTime(report.getEndTime());
				}

				if (result.getStartTime().after(report.getStartTime())) {
					result.setStartTime(report.getStartTime());
				}

				if (result.getEndTime().before(report.getEndTime())) {
					result.setEndTime(report.getEndTime());
				}
			}
		}

		return result;
	}

	private List<AbtestReport> queryReport(int runId, Date startTime, Date endTime) {
		List<AbtestReport> results = new ArrayList<AbtestReport>();

		try {
			List<com.dianping.cat.home.dal.abtest.AbtestReport> reports = m_abtestReportDao.findByRunIdDuration(runId,
			      startTime, endTime, AbtestReportEntity.READSET_FULL);

			for (com.dianping.cat.home.dal.abtest.AbtestReport report : reports) {
				String content = report.getContent();

				AbtestReport result = DefaultSaxParser.parse(content);
				results.add(result);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return results;
	}

	private Date resetTime(String period, Date time) {
		m_calendar.setTime(time);
		m_calendar.set(Calendar.MINUTE, 0);
		m_calendar.set(Calendar.SECOND, 0);
		m_calendar.set(Calendar.MILLISECOND, 0);

		if (period.equals("day")) {
			m_calendar.set(Calendar.HOUR_OF_DAY, 0);
		}

		return m_calendar.getTime();
	}

	class AbtestReportVisitor extends BaseVisitor {

		private AbtestReport m_report;

		private String m_variation = "";

		private Set<String> m_variationSet;

		private Map<String, MetricItemConfig> m_metricItemConfig;

		public AbtestReportVisitor(AbtestReport report) {
			m_report = report;
			m_variationSet = new HashSet<String>();

			m_variationSet.add("Control");
			m_variationSet.add("A");
			m_variationSet.add("B");
			m_variationSet.add("C");

			m_metricItemConfig = getMetricItemConfig();
		}

		public AbtestReport getReport() {
			return m_report;
		}

		@Override
		public void visitAbtestReport(AbtestReport abtestReport) {
			for (Goal goal : abtestReport.getGoals()) {
				String name = goal.getName();
				MetricItemConfig tmp = m_metricItemConfig.get(name);

				if (tmp.getViewOrder() > 0) {
					m_report.findOrCreateGoal(name);
				}
			}

			for (Variation variation : abtestReport.getVariations().values()) {
				if (m_variationSet.contains(variation.getName())) {
					visitVariation(variation);
				}
			}
		}

		@Override
		public void visitGoal(Goal goal) {
			String name = goal.getName();

			if (m_variation != null && m_variation.length() > 0) {
				Variation variation = m_report.findOrCreateVariation(m_variation);

				Goal result = variation.findOrCreateGoal(name);

				result.setType(goal.getType());
				result.setCount(result.getCount() + goal.getCount());
				result.setSum(result.getSum() + goal.getSum());
				// avg?
			}
		}

		@Override
		public void visitVariation(Variation variation) {
			m_variation = variation.getName();

			if (m_variation != null && m_variation.length() > 0) {
				m_report.findOrCreateVariation(m_variation);

				for (Goal goal : variation.getGoals().values()) {
					visitGoal(goal);
				}
			}
		}
	}

	enum DataSetColor {
		CONTROL(new DataSets("rgba(70, 136, 71,0.2)", "rgba(70, 136, 71,1)", "rgba(70, 136, 71, 1)", "#468847", null)),

		A(new DataSets("rgba(58, 135, 173, 0.3)", "rgba(58, 135, 173, 1)", "rgba(58, 135, 173, 1)", "#3a87ad", null)),

		B(new DataSets("rgba(185, 74, 72, 0.2)", "rgba(185, 74, 72, 1)", "rgba(185, 74, 72, 1)", "#b94a48", null)),

		C(new DataSets("rgba(248, 148, 6,0.2)", "rgba(248, 148, 6,1)", "rgba(248, 148, 6, 1)", "#f89406", null));

		public static DataSetColor getDataSetColor(String variation) {
			if (variation.equalsIgnoreCase("control")) {
				return CONTROL;
			} else if (variation.equalsIgnoreCase("A")) {
				return A;
			} else if (variation.equalsIgnoreCase("B")) {
				return B;
			} else if (variation.equalsIgnoreCase("C")) {
				return C;
			} else {
				return CONTROL;
			}
		}

		private DataSets m_dataSets;

		private DataSetColor(DataSets dataSets) {
			m_dataSets = dataSets;
		}

		public DataSets getDataSets() {
			return m_dataSets;
		}
	}

	@SuppressWarnings("unused")
	public static class DataSets {
		private String m_fillColor;

		private String m_strokeColor;

		private String m_pointColor;

		private String m_pointStrokeColor;

		private List<Number> m_data;

		public DataSets() {
		}

		public DataSets(String fillColor, String strokeColor, String pointColor, String pointStrokeColor,
		      List<Number> data) {
			super();
			m_fillColor = fillColor;
			m_strokeColor = strokeColor;
			m_pointColor = pointColor;
			m_pointStrokeColor = pointStrokeColor;
			m_data = data;
		}

		public String getPointStrokeColor() {
			return m_pointStrokeColor;
		}

		public void setData(List<Number> data) {
			m_data = data;
		}

		public String toJson() {
			Gson gson = m_gsonBuilderManager.getGsonBuilder().create();

			return gson.toJson(this, DataSets.class);
		}
	}

	static class DataSetsBuilder {
		public static DataSets buildDataSets(DataSetColor color, List<Number> number) {
			DataSets dataSets = color.getDataSets();
			dataSets.setData(number);

			return dataSets;
		}
	}
}
