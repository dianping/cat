package com.dianping.cat.report.page.problem;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

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

	/*
	 * private int getLastMinute(ProblemReport report, String ip) { Machine
	 * machine = report.findMachine(ip); int lastMinute = 0;
	 * 
	 * for (JavaThread thread : machine.getThreads().values()) { for (Segment
	 * segment : thread.getSegments().values()) { if (segment.getId() >
	 * lastMinute) { lastMinute = segment.getId(); } } }
	 * 
	 * return lastMinute; }
	 */

	private ProblemReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("ip", payload.getIpAddress()) //
		      .setProperty("thread", payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_service.invoke(request);
			ProblemReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligible service registered for " + request + "!");
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

		// init session
		HttpSession session = ctx.getHttpServletRequest().getSession();
		String sessionDomain = (String) session.getAttribute("domain");
		String sessionDate = (String) session.getAttribute("date");
		if (StringUtils.isEmpty(payload.getDomain()) && sessionDomain != null) {
			payload.setDomain(sessionDomain);
		}
		if (payload.getRealDate() == 0 && sessionDate != null) {
			payload.setDate(sessionDate);
		}

		model.setAction(payload.getAction());
		model.setPage(ReportPage.PROBLEM);
		model.setDisplayDomain(payload.getDomain());
		model.setIpAddress(payload.getIpAddress());

		ProblemReport report;
		switch (payload.getAction()) {
		case GROUP:
			report = showSummary(model, payload);
			if (report != null) {
				model.setGroupLevelInfo(new GroupLevelInfo(model).display(report));
			}
			model.setAllStatistics(new ProblemStatistics().displayAll(report, model));
			break;
		case THREAD:
			String groupName = payload.getGroupName();

			report = showSummary(model, payload);
			model.setGroupName(groupName);

			if (report != null) {
				model.setThreadLevelInfo(new ThreadLevelInfo(model, groupName).display(report));
			}

			model.setAllStatistics(new ProblemStatistics().displayAll(report, model));
			break;
		case DETAIL:
			showDetail(model, payload);
			break;
		}
		// reset session
		session.setAttribute("domain", model.getDomain());
		session.setAttribute("date", model.getDate());

		m_jspViewer.view(ctx, model);
	}

	private void showDetail(Model model, Payload payload) {
		model.setLongDate(payload.getDate());
		model.setIpAddress(payload.getIpAddress());
		model.setGroupName(payload.getGroupName());
		model.setCurrentMinute(payload.getMinute());
		model.setThreadId(payload.getThreadId());
		ProblemReport report = getReport(payload);

		if (report == null) {
			return;
		}
		model.setReport(report);
		model.setProblemStatistics(new ProblemStatistics().display(report, model));
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
