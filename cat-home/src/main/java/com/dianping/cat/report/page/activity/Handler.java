package com.dianping.cat.report.page.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.activity.entity.Activity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.transaction.TransactionMergeHelper;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.ActivityConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ActivityConfigManager m_configManager;

	@Inject
	private TransactionMergeHelper m_mergeHelper;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_service;

	private LinkedHashMap<String, TransactionReport> m_reports = new LinkedHashMap<String, TransactionReport>() {

		private static final long serialVersionUID = -3709193859176547991L;

		@Override
		protected boolean removeEldestEntry(Entry<String, TransactionReport> eldest) {
			return size() > 100;
		}
	};

	private TransactionReport fetchReport(Activity activity, long time) {
		String domain = activity.getDomain();
		String type = activity.getType();
		String name = activity.getName();

		if (name == null || name.length() == 0) {
			name = "*";
		}
		ModelPeriod period = ModelPeriod.getByTime(time);
		String key = String.valueOf(time) + ":" + domain;

		if (period.isHistorical()) {
			TransactionReport report = m_reports.get(key);

			if (report != null) {
				return report;
			}
		}
		ModelRequest request = new ModelRequest(domain, time) //
		      .setProperty("type", type) //
		      .setProperty("name", name);
		ModelResponse<TransactionReport> response = m_service.invoke(request);
		TransactionReport report = response.getModel();

		if (period.isLast() && report != null) {
			m_reports.put(key, report);
		}
		return report;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "activity")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "activity")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Date start = payload.getStartDate();
		Date end = payload.getEndDate();
		List<Activity> activities = m_configManager.getActivityConfig().getActivities();
		Map<String, List<LineChart>> chartsMap = new LinkedHashMap<String, List<LineChart>>();

		for (Activity activity : activities) {
			List<LineChart> charts = buildLineChart(activity, start, end);

			chartsMap.put(activity.getTitle(), charts);
		}
		model.setCharts(chartsMap);
		model.setStart(start);
		model.setEnd(end);
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.ACTIVITY);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	public List<LineChart> buildLineChart(Activity activity, Date start, Date end) {
		String type = activity.getType();
		String name = activity.getName();
		int size = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_MINUTE);
		LineChart countChart = new LineChart().setTitle("count (minute)").setStep(TimeHelper.ONE_MINUTE)
		      .setId(type + "_" + name + "_qps").setSize(size).setStart(start);
		LineChart avgChart = new LineChart().setTitle("response time(ms)").setStep(TimeHelper.ONE_MINUTE)
		      .setId(type + "_" + name + "_avg").setSize(size).setStart(start);
		List<LineChart> charts = new ArrayList<LineChart>();
		Double[] allCounts = new Double[size];
		Double[] allAvgs = new Double[size];
		long current = start.getTime();
		int index = 0;

		for (; current < end.getTime(); current = current + TimeHelper.ONE_HOUR) {
			TransactionReport report = fetchReport(activity, current);

			if (StringUtils.isEmpty(name)) {
				m_mergeHelper.mergerAllName(report, Constants.ALL, name);
			}
			TransactionReportVisitor visitor = new TransactionReportVisitor(type, name);

			visitor.visitTransactionReport(report);

			Double[] counts = visitor.getCount();
			Double[] avgs = visitor.getAvg();

			for (int i = 0; i < 60; i++, index++) {
				allCounts[index] = counts[i];
				allAvgs[index] = avgs[i];
			}
		}

		countChart.add("qps", allCounts);
		avgChart.add("avg", allAvgs);
		charts.add(countChart);
		charts.add(avgChart);
		return charts;
	}

	public class TransactionReportVisitor extends BaseVisitor {

		private String m_type;

		private String m_name;

		private Double[] m_count = new Double[60];

		private Double[] m_avg = new Double[60];

		public TransactionReportVisitor(String type, String name) {
			m_type = type;
			m_name = name;
		}

		@Override
		public void visitMachine(Machine machine) {
			super.visitMachine(machine);
		}

		@Override
		public void visitName(TransactionName name) {
			if (StringUtils.isEmpty(m_name) || m_name.equals(name.getId()))
				super.visitName(name);
		}

		@Override
		public void visitRange(Range range) {
			int id = range.getValue();

			m_count[id] = (double) range.getCount();
			m_avg[id] = range.getAvg();
		}

		@Override
		public void visitType(TransactionType type) {
			if (m_type.equals(type.getId())) {
				super.visitType(type);
			}
		}

		public Double[] getCount() {
			return m_count;
		}

		public Double[] getAvg() {
			return m_avg;
		}

	}

}
