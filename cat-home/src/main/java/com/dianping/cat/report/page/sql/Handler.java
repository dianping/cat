package com.dianping.cat.report.page.sql;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.NormalizePayload;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;

/**
 * @author youyong
 */
public class Handler implements PageHandler<Context> {

	@Inject
	private ReportService m_reportService;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private NormalizePayload m_normalizePayload;

	@Inject(type = ModelService.class, value = "sql")
	private ModelService<SqlReport> m_service;

	private SqlReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("database", payload.getDatabase());

		if (m_service.isEligable(request)) {
			ModelResponse<SqlReport> response = m_service.invoke(request);
			SqlReport report = response.getModel();

			if (payload.getPeriod().isLast()) {
				Set<String> domains = m_reportService.queryAllDomainNames(new Date(payload.getDate()),
				      new Date(payload.getDate() + TimeUtil.ONE_HOUR), "sql");
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable sql service registered for " + request + "!");
		}
	}

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

		normalize(model, payload);
		String database = payload.getDatabase();

		switch (payload.getAction()) {
		case HISTORY_REPORT:
			SqlReport historyReport = showSummarizeReport(model, payload);

			long historyDuration = historyReport.getEndTime().getTime() - historyReport.getStartTime().getTime();
			DisplaySqlReport displayHistorySql = new DisplaySqlReport().setDatabase(database).setDuration(historyDuration);

			displayHistorySql.setSortBy(payload.getSortBy()).visitSqlReport(historyReport);
			model.setReport(historyReport);
			model.setDisplaySqlReport(displayHistorySql);
			break;
		case HOURLY_REPORT:
			long hourlyDuration = TimeUtil.ONE_HOUR;
			if (ModelPeriod.CURRENT == payload.getPeriod()) {
				hourlyDuration = System.currentTimeMillis() % TimeUtil.ONE_HOUR;
			}
			SqlReport hourlyReport = getHourlyReport(payload);
			DisplaySqlReport displaySql = new DisplaySqlReport().setDatabase(database).setDuration(hourlyDuration);

			displaySql.setSortBy(payload.getSortBy()).visitSqlReport(hourlyReport);
			model.setReport(hourlyReport);
			model.setDisplaySqlReport(displaySql);
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.SQL);
		m_normalizePayload.normalize(model, payload);
		if (StringUtils.isEmpty(payload.getDatabase())) {
			payload.setDatabase(CatString.ALL);
		}
		model.setDatabase(payload.getDatabase());
	}

	private SqlReport showSummarizeReport(Model model, Payload payload) {
		String domain = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.querySqlReport(domain, start, end);
	}
}
