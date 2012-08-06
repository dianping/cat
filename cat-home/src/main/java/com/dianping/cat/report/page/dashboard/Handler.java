package com.dianping.cat.report.page.dashboard;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.google.gson.Gson;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject(type = ModelService.class, value = "event")
	private ModelService<EventReport> m_eventService;

	private Gson m_gson = new Gson();

	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "transaction")
	private ModelService<TransactionReport> m_transactionService;

	private EventReport getEventHourlyReport(String domain) {
		ModelRequest request = new ModelRequest(domain, ModelPeriod.CURRENT) //
		      .setProperty("ip", "All");

		if (m_transactionService.isEligable(request)) {
			ModelResponse<EventReport> response = m_eventService.invoke(request);
			EventReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	private TransactionReport getTransactionHourlyReport(String domain) {
		ModelRequest request = new ModelRequest(domain, ModelPeriod.CURRENT) //
		      .setProperty("ip", "All");

		if (m_transactionService.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_transactionService.invoke(request);
			TransactionReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "dashboard")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "dashboard")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.DASHBOARD);
		TransactionReport catReport = getTransactionHourlyReport("Cat");
		Set<String> domains = catReport.getDomainNames();
		Map<String, String> data = new HashMap<String, String>();
		data.put("timestamp", String.valueOf(new Date().getTime()));

		TransactionReport report = null;
		for (String domain : domains) {
			if (domain.equals("Cat")) {
				report = catReport;
			} else {
				report = getTransactionHourlyReport(domain);
			}
			Machine machine = report.getMachines().get("All");
			if (machine != null) {
				TransactionType urlDetail = machine.getTypes().get("URL");
				if (urlDetail != null) {
					data.put(domain + "UrlTotal", String.valueOf(urlDetail.getTotalCount()));
					data.put(domain + "UrlResponse", String.valueOf(urlDetail.getAvg()));
				}
				TransactionType serviceDetail = machine.getTypes().get("Service");
				if (serviceDetail != null) {
					data.put(domain + "ServiceTotal", String.valueOf(serviceDetail.getTotalCount()));
					data.put(domain + "ServiceResponse", String.valueOf(serviceDetail.getAvg()));

				}
			}
		}

		EventReport eventReport = null;
		for (String domain : domains) {
			eventReport = getEventHourlyReport(domain);
			com.dianping.cat.consumer.event.model.entity.Machine machine = eventReport.getMachines().get("All");
			if (machine != null) {
				EventType exceptionDetail = machine.getTypes().get("Exception");
				if (exceptionDetail != null) {
					data.put(domain + "Exception", String.valueOf(exceptionDetail.getTotalCount()));
				}
			}
		}

		model.setData(m_gson.toJson(data));
		m_jspViewer.view(ctx, model);
	}
}
