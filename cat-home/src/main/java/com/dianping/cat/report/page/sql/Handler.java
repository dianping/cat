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
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.SQL);

		Payload payload = ctx.getPayload();
		SqlReport report = new SqlReport();
		String domain = payload.getDomain();
		long startDate = payload.getDate();
		model.setDate(startDate);
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
			if (domain == null && domains.size() > 0) {
				domain = domains.get(0);
			}

			List<SqlReportRecord> reportRecords = m_dao.findAllByDomainAndDate(domain, transactiondate, SqlReportRecordEntity.READSET_FULL);
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

		m_jspViewer.view(ctx, model);
	}
}
