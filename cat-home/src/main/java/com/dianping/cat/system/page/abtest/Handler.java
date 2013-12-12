package com.dianping.cat.system.page.abtest;

import java.io.IOException;

import javax.servlet.ServletException;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.abtest.handler.ABTestHandler;
import com.dianping.cat.system.page.abtest.handler.AdvisorHandler;
import com.dianping.cat.system.page.abtest.handler.GroupStrategyHandler;
import com.dianping.cat.system.page.abtest.handler.ListViewHandler;
import com.dianping.cat.system.page.abtest.handler.ModelHandler;
import com.dianping.cat.system.page.abtest.handler.ReportHandler;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.dianping.cat.system.page.abtest.util.GsonManager;
import com.google.gson.Gson;

public class Handler implements PageHandler<Context>, Initializable {

	public static final String CHARSET = "UTF-8";

	@Inject
	private JspViewer m_jspViewer;
	
	@Inject
	private ABTestService m_service;

	@Inject
	private GroupStrategyHandler m_groupStrategyHandler;

	@Inject
	private ABTestHandler m_abtestHandler;

	@Inject
	private ListViewHandler m_listViewHandler;

	@Inject
	private ReportHandler m_reportHandler;

	@Inject
	private ModelHandler m_modelHandler;

	@Inject
	private AdvisorHandler m_advisorHandler;

	@Inject
	private GsonManager m_gsonBuilderManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "abtest")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		if (ctx.getException() != null) {
			ctx.setResponseJson(responseJson(1, ctx.getException().getMessage()));
			return;
		}

		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		if (action == Action.VIEW) {
			m_listViewHandler.handleInbound(ctx, payload);
		}

		if (ctx.getHttpServletRequest().getMethod().equalsIgnoreCase("post")) {
			if (action == Action.AJAX_CREATE || action == Action.AJAX_DETAIL) {
				m_abtestHandler.handleInbound(ctx, payload);
			} else if (action == Action.AJAX_ADDGROUPSTRATEGY || action == Action.AJAX_PARSEGROUPSTRATEGY) {
				m_groupStrategyHandler.handleInbound(ctx, payload);
			} else if (action == Action.ABTEST_CACULATOR) {
				m_advisorHandler.handleInbound(ctx, payload);
			}
		}
	}

	@Override
	@OutboundActionMeta(name = "abtest")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case VIEW:
			m_listViewHandler.handleOutbound(ctx, model, payload);
			break;
		case CREATE:
			m_abtestHandler.handleOutbound(ctx, model, payload);
			break;
		case DETAIL:
			m_abtestHandler.handleOutbound(ctx, model, payload);
			break;
		case REPORT:
			m_reportHandler.handleOutbound(ctx, model, payload);
			break;
		case MODEL:
			m_modelHandler.handleOutbound(ctx, model, payload);
			break;
		}

		model.setAction(action);
		model.setPage(SystemPage.ABTEST);
		m_jspViewer.view(ctx, model);
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_service instanceof Task) {
			Threads.forGroup("Cat").start((Task) m_service);
		}
	}

	/**
	 * 
	 * @param code
	 *           0 for success, 1 for failure
	 * @param msg
	 * @return
	 */
	public String responseJson(int code, String msg) {
		Gson gson = m_gsonBuilderManager.getGson();
		return gson.toJson(new ResponseJson(code, msg), ResponseJson.class);
	}
}
