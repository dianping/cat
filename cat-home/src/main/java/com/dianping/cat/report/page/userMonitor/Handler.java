package com.dianping.cat.report.page.userMonitor;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.config.UrlPatternConfigManager;
import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	@Inject
	private CityManager m_cityManager;
	
	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "userMonitor")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "userMonitor")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);
		List<PatternItem> items = m_patternManager.queryUrlPatternRules();
		
		
		model.setCities(m_cityManager.getCities());
		model.setPattermItems(items);
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.USERMONITOR);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
	
	
	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.USERMONITOR);

		m_normalizePayload.normalize(model, payload);
	}
}
