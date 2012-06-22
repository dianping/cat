package com.dianping.cat.report.page.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import com.dianping.cat.helper.DateDeserializer;
import com.dianping.cat.job.sql.dal.SqlReportRecord;
import com.dianping.cat.job.sql.dal.SqlReportRecordDao;
import com.dianping.cat.job.sql.dal.SqlReportRecordEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.site.dal.jdbc.DalException;
import com.site.dal.jdbc.Readset;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private SqlReportRecordDao m_dao;

	@Inject
	private GraphBuilder m_builder;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "sql")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "sql")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setPage(ReportPage.SQL);
		model.setDisplayDomain(payload.getDomain());
		model.setAction(payload.getAction());
		// Last hour is default
		if (payload.getPeriod().isCurrent()) {
			payload.setStep(payload.getStep() - 1);
		}
		switch (payload.getAction()) {
		case VIEW:
			showReport(model, payload);
			break;
		case GRAPHS:
			showGraphs(model, payload);
			break;
		case MOBLIE:
			showReport(model, payload);
			SqlReport report = model.getReport();
			Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateDeserializer()).create();
			model.setMobileResponse(gson.toJson(report));
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	protected void showGraphs(Model model, Payload payload) {
		int id = payload.getId();
		try {
			String statement = "";
			String durationDistribution = "";
			String durationOvertime = "";
			String hitsovOvrtime = "";
			String failureOvertime = "";
			// when id is 0, will analyze all the data under this domain
			if (id == 0) {
				List<SqlReportRecord> allRecords = m_dao.findAllByDomainAndDate(payload.getDomain(),
				      new Date(payload.getDate()), SqlReportRecordEntity.READSET_FULL);
				List<String> durationDistributions = new ArrayList<String>();
				List<String> durationOvertimes = new ArrayList<String>();
				List<String> hitsovOvrtimes = new ArrayList<String>();
				List<String> failureOvertimes = new ArrayList<String>();
				for (SqlReportRecord record : allRecords) {
					durationDistributions.add(record.getDurationDistribution());
					durationOvertimes.add(record.getDurationOverTime());
					hitsovOvrtimes.add(record.getHitsOverTime());
					failureOvertimes.add(record.getFailureOverTime());
				}
				// summary all the result
				durationDistribution = compute(durationDistributions);
				hitsovOvrtime = compute(hitsovOvrtimes);
				failureOvertime = compute(failureOvertimes);
				durationOvertime = computeDuration(durationOvertimes, hitsovOvrtimes);
			} else {
				SqlReportRecord record = m_dao.findByPK(id, SqlReportRecordEntity.READSET_FULL);
				statement = record.getStatement();
				durationDistribution = record.getDurationDistribution();
				durationOvertime = record.getDurationOverTime();
				hitsovOvrtime = record.getHitsOverTime();
				failureOvertime = record.getFailureOverTime();
			}

			String graph1 = m_builder.build(new SqlGraphPayload(0, "SQL Exeture Time Distribution", "Duration (ms)",
			      "Count", durationDistribution));
			String graph2 = m_builder.build(new SqlGraphPayload(1, "SQL Hits Over One Hour", "Time (min)", "Count",
			      hitsovOvrtime));
			String graph3 = m_builder.build(new SqlGraphPayload(2, "SQL Exeture Average Time Over One Hour", "Time (min)",
			      "Average Duration (ms)", durationOvertime));
			String graph4 = m_builder.build(new SqlGraphPayload(3, "SQL Failures Over One Hour", "Time (min)", "Count",
			      failureOvertime));

			model.setGraph1(graph1);
			model.setGraph2(graph2);
			model.setGraph3(graph3);
			model.setGraph4(graph4);
			model.setStatement(statement);
		} catch (DalException e) {
			e.printStackTrace();
		}
	}

	protected void showReport(Model model, Payload payload) {
		SqlReport report = new SqlReport();
		report.setSortBy(payload.getSortBy());
		String domain = payload.getDomain();
		long startDate = payload.getDate();
		model.setLongDate(startDate);
		Date transactiondate = new Date(startDate);
		List<String> domains = new ArrayList<String>();
		Readset<SqlReportRecord> domainSet = SqlReportRecordEntity.READSET_DOMAIN;

		List<SqlReportModel> sqlRecordModels = new ArrayList<SqlReportModel>();
		try {
			List<SqlReportRecord> recordsForDomain = m_dao.findAllDistinctByDate(transactiondate, domainSet);

			if (recordsForDomain != null) {
				for (SqlReportRecord record : recordsForDomain) {
					domains.add(record.getDomain());
				}
			} else {
				if (domain != null) {
					domains.add(domain);
				}
			}
			if ((domain == null || domain.length() == 0) && domains.size() > 0) {
				domain = domains.get(0);
			}

			List<SqlReportRecord> reportRecords = m_dao.findAllByDomainAndDate(domain, transactiondate,
			      SqlReportRecordEntity.READSET_FULL);
			if (reportRecords != null) {
				for (SqlReportRecord record : reportRecords) {
					sqlRecordModels.add(new SqlReportModel(record));
				}
			}

			if (domain != null) {
				reportRecords = m_dao.findAllByDomainAndDate(domain, transactiondate, SqlReportRecordEntity.READSET_FULL);
			}
			report.setDomain(domain).setDomains(domains).setReportRecords(sqlRecordModels);
			report.setStartTime(new Date(startDate)).setEndTime(new Date(startDate + 60 * 60 * 1000 - 100));
			model.setReport(report);
		} catch (DalException e) {
			e.printStackTrace();
		}
	}

	// the computation of distribution is just a sum of all the date under the
	// same domain and date
	// String format key:value,key:value,... type of key and value int
	public String compute(List<String> durationDistributions) {
		Map<Integer, Integer> durations = new TreeMap<Integer, Integer>();
		for (String s : durationDistributions) {
			String distrubutions[] = s.split(",");
			for (int i = 0; i < distrubutions.length; i++) {
				String singleResult[] = distrubutions[i].split(":");
				int duration = Integer.parseInt(singleResult[0]);
				int count = Integer.parseInt(singleResult[1]);
				Integer value = durations.get(duration);
				durations.put(duration, value == null ? count : value + count);
			}
		}
		StringBuilder sb = new StringBuilder();
		Iterator<Entry<Integer, Integer>> it = durations.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) it.next();
			int key = entry.getKey();
			int count = entry.getValue();
			String result = key + ":" + count + ",";
			sb.append(result);
		}
		return sb.substring(0, sb.length() - 1);
	}

	// the computation of duration is:duration=(hit*duration)/hit
	public String computeDuration(List<String> durationOvertimes, List<String> hitsovOvrtimes) {
		double[] sum = new double[13];
		int[] totalHit = new int[13];
		double[] average = new double[13];
		for (int i = 0; i < durationOvertimes.size(); i++) {
			String durations[] = durationOvertimes.get(i).split(",");
			String hits[] = hitsovOvrtimes.get(i).split(",");
			for (int j = 0; j <= 12; j++) {
				String s_duration = durations[j].split(":")[1];
				String s_hit = hits[j].split(":")[1];
				double i_duration = Double.parseDouble(s_duration);
				int i_hit = Integer.parseInt(s_hit);
				totalHit[j] += i_hit;
				sum[j] += i_duration * i_hit;
			}
		}
		for (int m = 0; m <= 12; m++) {
			average[m] = totalHit[m] == 0 ? 0 : sum[m] /(double) totalHit[m];
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= 12; i++) {
			String s = i * 5 + ":" + average[i] + ",";
			sb.append(s);
		}
		return sb.substring(0, sb.length() - 1);
	}
}
