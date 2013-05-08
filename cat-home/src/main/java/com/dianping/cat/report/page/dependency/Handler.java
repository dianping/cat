package com.dianping.cat.report.page.dependency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "dependency")
	private ModelService<DependencyReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	private DependencyReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date);

		if (m_service.isEligable(request)) {
			ModelResponse<DependencyReport> response = m_service.invoke(request);
			DependencyReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable dependency service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "dependency")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "dependency")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);

		DependencyReport report = getReport(payload);
		String min = payload.getMinute();
		int minute = 0;

		Set<Integer> keys = report.getSegments().keySet();
		List<Integer> minutes = new ArrayList<Integer>(keys);
		Collections.sort(minutes);

		if (StringUtils.isEmpty(min)) {
			if (minutes.size() > 0) {
				min = String.valueOf(minutes.get(minutes.size() - 1));
				minute = Integer.parseInt(min);
			}
		}else{
			minute = Integer.parseInt(min);
		}
		model.setMinute(minute);
		model.setMinutes(minutes);
		model.setReport(report);
		model.setSegment(report.findSegment(minute));
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.DEPENDENCY);
		model.setAction(Action.VIEW);

		m_normalizePayload.normalize(model, payload);
	}

}
