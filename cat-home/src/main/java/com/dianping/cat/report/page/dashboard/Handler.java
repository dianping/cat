package com.dianping.cat.report.page.dashboard;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

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
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "transaction")
	private ModelService<TransactionReport> m_service;

	private Gson m_gson = new Gson();

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
		TransactionReport catReport = getHourlyReport("Cat");
		Set<String> domains = catReport.getDomainNames();
		Map<String, String> data = new HashMap<String, String>();
		data.put("timestamp", sdf.format(new Date()));

		TransactionReport report = null;
		for (String domain : domains) {
			if (domain.equals("Cat")) {
				report = catReport;
			} else {
				report = getHourlyReport(domain);
			}
			Machine machine = report.getMachines().get("All");
			if (machine != null) {
				TransactionType detail = machine.getTypes().get("URL");
				if (detail != null) {
					data.put(domain + "UrlTotal", String.valueOf(detail.getTotalCount()));
					data.put(domain + "UrlResponse", String.valueOf(detail.getAvg()));
				}
			}
		}

		model.setData(m_gson.toJson(data));
		m_jspViewer.view(ctx, model);
	}

	private TransactionReport getHourlyReport(String domain) {
		ModelRequest request = new ModelRequest(domain, ModelPeriod.CURRENT) //
		      .setProperty("ip", "All");

		if (m_service.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_service.invoke(request);
			TransactionReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}
}
