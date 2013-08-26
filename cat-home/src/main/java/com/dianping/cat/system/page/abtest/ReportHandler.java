package com.dianping.cat.system.page.abtest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
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
import com.dianping.cat.system.page.abtest.ListViewModel.AbtestItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ReportHandler implements SubHandler {

	@Inject
	private AbtestDao m_abtestDao;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	@Inject
	private AbtestReportDao m_abtestReportDao;

	private static GsonBuilderManager m_gsonBuilderManager = new GsonBuilderManager();

	private DateFormat m_dateFormat = new SimpleDateFormat("MM-dd HH:00");

	private Calendar m_calendar = Calendar.getInstance();

	private AbtestReport buildDailyReport(AbtestReport query, String goal) {
		Date startTime = query.getStartTime();
		Date endTime = query.getEndTime();
		m_calendar.setTime(startTime);

		long deltaTime = endTime.getTime() - startTime.getTime();
		long day = (deltaTime + 1000L) / (24 * 60 * 60 * 1000);

		day = (day > 7) ? day : 7;
		List<AbtestReport> datas = new ArrayList<AbtestReport>();

		for (int i = 0; i < day; i++) {
			m_calendar.add(Calendar.DAY_OF_MONTH, 1);
			endTime = m_calendar.getTime();

			List<AbtestReport> reports = queryReport(query.getRunId(), startTime, endTime);

			AbtestReport report = mergeReport(reports);

			datas.add(report);

			startTime = endTime;
		}

		Chart chart = new Chart();
		String datasets = buildDateSets(datas, goal);
		String labels = "[]";
		if (datas.size() > 0) {
			AbtestReport first = datas.get(0);

			labels = buildLabel("day", first.getStartTime(), datas.size());
		}

		chart.setType("day");
		chart.setLabels(labels);
		chart.setDatasets(datasets);
		chart.setGoal(goal);

		AbtestReport report = mergeReport(datas);

		report.setChart(chart);
		report.setRunId(query.getRunId());
		report.setStartTime(query.getStartTime());
		report.setEndTime(query.getEndTime());

		return report;
	}

	private String buildDateSets(List<AbtestReport> reports, String goal) {
		List<DataSets> dataSets = new ArrayList<DataSets>();
		String datasets = "[]";

		if (reports.size() > 0) {
			Set<String> keys = reports.get(0).getVariations().keySet();

			for (String key : keys) {
				List<Number> data = new ArrayList<Number>();

				for (AbtestReport report : reports) {
					if (report.getStartTime() != null) {
						Variation variation = report.findOrCreateVariation(key);

						Goal tmp = variation.findOrCreateGoal(goal);

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
				}

				DataSets dataSet = DataSetsBuilder.buildDataSets(DataSetColor.getDataSetColor(key), data);
				dataSets.add(dataSet);
			}

			Gson gson = m_gsonBuilderManager.getGsonBuilder().create();

			datasets = gson.toJson(dataSets, new TypeToken<List<DataSets>>() {
			}.getType());
		}

		return datasets;
	}

	private AbtestReport buildHourlyReport(AbtestReport query, String goal) {
		List<AbtestReport> reports = queryReport(query.getRunId(), query.getStartTime(), query.getEndTime());

		Chart chart = new Chart();
		String datasets = buildDateSets(reports, goal);
		String labels = "[]";
		if (reports.size() > 0) {
			AbtestReport first = reports.get(0);

			labels = buildLabel("hour", first.getStartTime(), reports.size());
		}

		chart.setType("hour");
		chart.setLabels(labels);
		chart.setDatasets(datasets);
		chart.setGoal(goal);

		AbtestReport report = mergeReport(reports);

		report.setChart(chart);
		report.setRunId(query.getRunId());
		report.setStartTime(query.getStartTime());
		report.setEndTime(query.getEndTime());

		return report;
	}

	private String buildLabel(String period, Date startTime, int num) {
		List<String> labels = new ArrayList<String>();
		m_calendar.setTime(startTime);

		if (period.equals("hour")) {
			int hour = (num > 24) ? num : 24;

			for (int i = 0; i < hour; i++) {
				labels.add(m_dateFormat.format(startTime));
				labels.add("");
				m_calendar.add(Calendar.HOUR, 2);
				i++;
				startTime = m_calendar.getTime();
			}

		} else {
			int day = (num > 7) ? num : 7;

			for (int i = 0; i < day; i++) {
				labels.add(m_dateFormat.format(startTime));
				m_calendar.add(Calendar.DAY_OF_MONTH, 1);

				startTime = m_calendar.getTime();
			}
		}

		Gson gson = m_gsonBuilderManager.getGsonBuilder().create();

		String label = gson.toJson(labels, new TypeToken<List<String>>() {
		}.getType());

		return label;
	}

	private AbtestReport buildQuery(int runId, Date startTime, Date endTime, String period) {
		AbtestReport query = new AbtestReport();
		Date now = new Date();

		query.setRunId(runId);

		if (startTime == null && endTime == null) {
			m_calendar.setTime(now);

			if (period.equals("day")) {
				m_calendar.add(Calendar.DAY_OF_MONTH, -7);
			} else {
				m_calendar.add(Calendar.HOUR_OF_DAY, -24);
			}

			query.setStartTime(m_calendar.getTime());
			query.setEndTime(now);
		} else if (endTime == null) {
			m_calendar.setTime(startTime);

			if (period.equals("day")) {
				m_calendar.add(Calendar.DAY_OF_MONTH, 7);
			} else {
				m_calendar.add(Calendar.HOUR_OF_DAY, 24);
			}

			query.setStartTime(startTime);
			query.setEndTime(m_calendar.getTime());
		} else if (startTime == null) {
			m_calendar.setTime(endTime);

			if (period.equals("day")) {
				m_calendar.add(Calendar.DAY_OF_MONTH, -7);
			} else {
				m_calendar.add(Calendar.HOUR_OF_DAY, -24);
			}

			query.setStartTime(m_calendar.getTime());
			query.setEndTime(endTime);
		} else {
			query.setStartTime(startTime);
			query.setEndTime(endTime);
		}

		return query;
	}

	public AbtestReport buildReport(AbtestReport query, String goal, String period) {
		AbtestReport report = null;

		if (period.equals("day")) {
			report = buildDailyReport(query, goal);
		} else {
			report = buildHourlyReport(query, goal);
		}

		return report;
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

			AbtestReport query = buildQuery(runId, startTime, endTime, period);
			AbtestReport report = buildReport(query, goal, period);

			model.setAbtest(item);
			model.setReport(report);
		} catch (Exception e) {
			Cat.logError(e);
			e.printStackTrace();
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

	class AbtestReportVisitor extends BaseVisitor {

		private AbtestReport m_report;

		private String m_variation = "";

		public AbtestReportVisitor(AbtestReport report) {
			m_report = report;
		}

		public AbtestReport getReport() {
			return m_report;
		}

		@Override
		public void visitGoal(Goal goal) {
			String name = goal.getName();

			if (name != null) {
				m_report.findOrCreateGoal(goal.getName());

				if (m_variation != null && m_variation.length() > 0) {
					Variation variation = m_report.findOrCreateVariation(m_variation);
					Goal result = variation.findOrCreateGoal(goal.getName());

					result.setType(goal.getType());
					result.setCount(result.getCount() + goal.getCount());
					result.setSum(result.getSum() + goal.getSum());
				}
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

		C(new DataSets("rgba(248, 148, 6,0.2)", "rgba(248, 148, 6,1)", "rgba(248, 148, 6, 1)", "#f89406", null)),

		D(new DataSets("rgba(153, 153, 153, 0.2)", "rgba(153, 153, 153, 1)", "rgba(153, 153, 153, 1)", "#999999", null));

		private DataSets m_dataSets;

		public static DataSetColor getDataSetColor(String variation) {
			if (variation.equalsIgnoreCase("control")) {
				return CONTROL;
			} else if (variation.equalsIgnoreCase("A")) {
				return A;
			} else if (variation.equalsIgnoreCase("B")) {
				return B;
			} else {
				return CONTROL;
			}
		}

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

		private String pointStrokeColor;

		private List<Number> m_data;

		public DataSets() {
		}

		public DataSets(String fillColor, String strokeColor, String pointColor, String pointStrokeColor,
		      List<Number> data) {
			super();
			m_fillColor = fillColor;
			m_strokeColor = strokeColor;
			m_pointColor = pointColor;
			this.pointStrokeColor = pointStrokeColor;
			m_data = data;
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
