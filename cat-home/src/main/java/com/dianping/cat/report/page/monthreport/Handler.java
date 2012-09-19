package com.dianping.cat.report.page.monthreport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.monthreport.model.entity.MonthReport;
import com.dianping.cat.consumer.monthreport.model.transform.DefaultSaxParser;
import com.dianping.cat.home.dal.report.Monthreport;
import com.dianping.cat.home.dal.report.MonthreportDao;
import com.dianping.cat.home.dal.report.MonthreportEntity;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private MonthreportDao m_monthreportDao;

	@Inject
	private ServerConfigManager m_manager;

	private MonthReport buildMonthReport(String domain, Date start) throws DalException, SAXException, IOException {
		try {
			Monthreport report = m_monthreportDao.findByDomainPeriod(domain, start, MonthreportEntity.READSET_FULL);
			String content = report.getContent();
			MonthReport monthreport = DefaultSaxParser.parse(content);
			return monthreport;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new MonthReport();
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "monthreport")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "monthreport")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(payload, model);
		try {
			Action action = payload.getAction();
			Date start = payload.getHistoryStartDate();
			Date end = payload.getHistoryEndDate();

			switch (action) {
			case VIEW:
				MonthReport report = buildMonthReport(payload.getDomain(), start);
				MonthReport reportLast = buildMonthReport(payload.getDomain(), getLastMonthDate(start, -1));
				MonthReport reportLastTwo = buildMonthReport(payload.getDomain(), getLastMonthDate(start, -2));

				model.setReport(report);
				model.setReportLast(reportLast);
				model.setReportLastTwo(reportLastTwo);
				break;
			case ALL:
				List<MonthReport> reports = new ArrayList<MonthReport>();
				List<String> domains = getAllDomains(start, end);
				for (String domain : domains) {
					MonthReport buildProblemReport = buildMonthReport(domain, start);
					reports.add(buildProblemReport);
				}
				model.setReports(reports);
				break;
			}

		} catch (Exception e) {
			Cat.logError(e);
		}
		m_jspViewer.view(ctx, model);
	}

	private Date getLastMonthDate(Date date, int step) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, step);
		return cal.getTime();
	}

	private List<String> getAllDomains(Date start, Date end) {
		List<String> domains = new ArrayList<String>();

		try {
			List<Monthreport> reports = m_monthreportDao.findByPeriod(start, MonthreportEntity.READSET_DOMAIN_PERIOD);

			for (Monthreport report : reports) {
				domains.add(report.getDomain());
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return domains;
	}

	private void normalize(Payload payload, Model model) {
		String domain = payload.getDomain();

		if (domain == null || domain.length() == 0) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}
		model.setDomain(payload.getDomain());
		model.setAction(payload.getAction());
		payload.setReportType("month");
		payload.computeStartDate();
		model.setLongDate(payload.getDate());
	}
}
