package com.dianping.cat.report.page.problem;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.ServletException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "problem")
	private ModelService<ProblemReport> m_service;

	@Inject
	private ServerConfigManager m_manager;

	private int getHour(long date) {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(date);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	private String getIpAddress(ProblemReport report, Payload payload) {
		Map<String, Machine> machines = report.getMachines();
		String ip = payload.getIpAddress();

		if ((ip == null || ip.length() == 0) && !machines.isEmpty()) {
			ip = machines.keySet().iterator().next();
		}

		return ip;
	}

	private ProblemReport getAllIpReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date);

		if (m_service.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_service.invoke(request);
			ProblemReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

	private ProblemReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("ip", payload.getIpAddress()) //
		      .setProperty("thread", payload.getThreadId());

		if (m_service.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_service.invoke(request);
			ProblemReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

	private void setDefaultThreshold(Model model, Payload payload) {
		Map<String, Domain> domains = m_manager.getLongUrlDomains();
		Domain d = domains.get(payload.getDomain());

		if (d != null) {
			int longUrlTime = d.getThreshold();

			if (longUrlTime != 500 && longUrlTime != 1000 && longUrlTime != 2000 && longUrlTime != 3000
			      && longUrlTime != 4000 && longUrlTime != 5000) {
				double sec = (double) (longUrlTime) / (double) 1000;
				NumberFormat nf = new DecimalFormat("#.##");
				String option = "<option value=\"" + longUrlTime + "\"" + ">" + nf.format(sec) + " Sec</option>";

				model.setDefaultThreshold(option);
			}
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "p")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "p")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}

		setDefaultThreshold(model, payload);

		Map<String, Domain> domains = m_manager.getLongUrlDomains();
		Domain d = domains.get(payload.getDomain());

		if (d != null && payload.getRealLongTime() == 0) {
			payload.setLongTime(d.getThreshold());
		}

		model.setAction(payload.getAction());
		model.setPage(ReportPage.PROBLEM);
		model.setDisplayDomain(payload.getDomain());
		model.setIpAddress(payload.getIpAddress());
		model.setThreshold(payload.getLongTime());

		ProblemReport report;
		String ip = payload.getIpAddress();

		if (ip == null || ip.length() == 0 || ip.equals("All")) {
			model.setIpAddress("All");
			report = getAllIpReport(payload);
			model.setReport(report);
			model.setLongDate(payload.getDate());
			model.setAllStatistics(new ProblemStatistics().displayByAllIps(report, payload));
		} else {
			switch (payload.getAction()) {
			case GROUP:
				report = showSummary(model, payload);
				if (report != null) {
					model.setGroupLevelInfo(new GroupLevelInfo(model).display(report));
				}
				model.setAllStatistics(new ProblemStatistics().displayByIp(report, model, payload));
				break;
			case THREAD:
				String groupName = payload.getGroupName();

				report = showSummary(model, payload);
				model.setGroupName(groupName);

				if (report != null) {
					model.setThreadLevelInfo(new ThreadLevelInfo(model, groupName).display(report));
				}

				model.setAllStatistics(new ProblemStatistics().displayByIp(report, model, payload));
				break;
			case DETAIL:
				showDetail(model, payload);
				break;
			}
		}

		m_jspViewer.view(ctx, model);
	}

	private void showDetail(Model model, Payload payload) {
		String ipAddress = payload.getIpAddress();
		model.setLongDate(payload.getDate());
		model.setIpAddress(ipAddress);
		model.setGroupName(payload.getGroupName());
		model.setCurrentMinute(payload.getMinute());
		model.setThreadId(payload.getThreadId());

		ProblemReport report = getReport(payload);

		if (report == null) {
			return;
		}
		model.setReport(report);
		model.setProblemStatistics(new ProblemStatistics().displayByGroupOrThread(report, model, payload));
	}

	private ProblemReport showSummary(Model model, Payload payload) {
		ModelPeriod period = payload.getPeriod();
		if (period.isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}

		if (period.isCurrent() || period.isFuture()) {
			Calendar cal = Calendar.getInstance();
			int minute = cal.get(Calendar.MINUTE);

			model.setLastMinute(minute);
		} else {
			model.setLastMinute(59);
		}

		model.setHour(getHour(model.getLongDate()));

		ProblemReport report = getReport(payload);

		if (report != null) {
			String ip = getIpAddress(report, payload);

			model.setIpAddress(ip);
			model.setReport(report);
		}

		return report;
	}
}
