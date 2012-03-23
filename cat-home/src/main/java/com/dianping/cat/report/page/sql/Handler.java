package com.dianping.cat.report.page.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.job.sql.dal.SqlReportRecord;
import com.dianping.cat.job.sql.dal.SqlReportRecordDao;
import com.dianping.cat.job.sql.dal.SqlReportRecordEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
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
		model.setPage(ReportPage.SQL);
		Payload payload = ctx.getPayload();

		Action action = payload.getAction();
		if (action == null || action == Action.VIEW) {
			model.setAction(Action.VIEW);
			showReport(model, payload);
		} else {
			model.setAction(Action.GRAPHS);
			showGraphs(model, payload);
		}
		m_jspViewer.view(ctx, model);
	}

	public void showGraphs(Model model, Payload payload) {
		int id = payload.getId();
		try {
			SqlReportRecord record = m_dao.findByPK(id, SqlReportRecordEntity.READSET_FULL);
			String statement = record.getStatement();
			String durationDistribution = record.getDurationDistribution();
			String durationOvertime = record.getDurationOverTime();
			String hitsovOvrtime = record.getHitsOverTime();
			String failureOvertime = record.getFailureOverTime();

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

	public void showReport(Model model, Payload payload) {
		SqlReport report = new SqlReport();
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
				reportRecords = m_dao.findAllByDomainAndDate(domain, transactiondate, domainSet);
			}
			report.setDomain(domain).setDomains(domains).setReportRecords(sqlRecordModels);
			report.setStartTime(new Date(startDate)).setEndTime(new Date(startDate + 60 * 60 * 1000 - 100));
			model.setReport(report);
		} catch (DalException e) {
			e.printStackTrace();
		}
	}
}
