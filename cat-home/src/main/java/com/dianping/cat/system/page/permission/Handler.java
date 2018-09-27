package com.dianping.cat.system.page.permission;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private UserConfigManager m_userConfigManager;

	@Inject
	private ResourceConfigManager m_resourceConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "permission")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "permission")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		model.setAction(action);
		model.setPage(SystemPage.PERMISSION);

		switch (action) {
		case USER:
			String userConfig = payload.getContent();
			if (!StringUtils.isEmpty(userConfig)) {
				model.setOpState(m_userConfigManager.insert(userConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_userConfigManager.getConfig().toString()));
			break;
		case RESOURCE:
			String resourceConfig = payload.getContent();
			if (!StringUtils.isEmpty(resourceConfig)) {
				model.setOpState(m_resourceConfigManager.insert(resourceConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_resourceConfigManager.getConfig().toString()));
			break;
		case ERROR:
			break;
		}
		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
