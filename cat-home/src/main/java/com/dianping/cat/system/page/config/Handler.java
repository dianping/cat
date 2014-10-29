package com.dianping.cat.system.page.config;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.unidal.web.mvc.annotation.PreInboundActionMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.ConfigModification;
import com.dianping.cat.home.dal.report.ConfigModificationDao;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.config.process.AlertConfigProcessor;
import com.dianping.cat.system.page.config.process.AppConfigProcessor;
import com.dianping.cat.system.page.config.process.ExceptionConfigProcessor;
import com.dianping.cat.system.page.config.process.GlobalConfigProcessor;
import com.dianping.cat.system.page.config.process.HeartbeatConfigProcessor;
import com.dianping.cat.system.page.config.process.MetricConfigProcessor;
import com.dianping.cat.system.page.config.process.NetworkConfigProcessor;
import com.dianping.cat.system.page.config.process.PatternConfigProcessor;
import com.dianping.cat.system.page.config.process.SystemConfigProcessor;
import com.dianping.cat.system.page.config.process.TopologyConfigProcessor;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private GlobalConfigProcessor m_globalConfigProcessor;

	@Inject
	private PatternConfigProcessor m_patternConfigProcessor;

	@Inject
	private TopologyConfigProcessor m_topologyConfigProcessor;

	@Inject
	private MetricConfigProcessor m_metricConfigProcessor;

	@Inject
	private ExceptionConfigProcessor m_exceptionConfigProcessor;

	@Inject
	private NetworkConfigProcessor m_networkConfigProcessor;

	@Inject
	private SystemConfigProcessor m_systemConfigProcessor;

	@Inject
	private HeartbeatConfigProcessor m_heartbeatConfigProcessor;

	@Inject
	private AppConfigProcessor m_appConfigProcessor;

	@Inject
	private AlertConfigProcessor m_alertConfigProcessor;

	@Inject
	private ConfigModificationDao m_configModificationDao;

	@Override
	@PreInboundActionMeta("login")
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "config")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "config")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setPage(SystemPage.CONFIG);
		Action action = payload.getAction();

		storeModifyInfo(ctx, payload);
		model.setAction(action);
		switch (action) {
		case PROJECT_ALL:
		case PROJECT_UPDATE:
		case PROJECT_UPDATE_SUBMIT:
		case PROJECT_DELETE:
		case DOMAIN_GROUP_CONFIG_UPDATE:
		case BUG_CONFIG_UPDATE:
		case THIRD_PARTY_CONFIG_UPDATE:
		case ROUTER_CONFIG_UPDATE:
			m_globalConfigProcessor.process(action, payload, model);
			break;

		case AGGREGATION_ALL:
		case AGGREGATION_UPDATE:
		case AGGREGATION_UPDATE_SUBMIT:
		case AGGREGATION_DELETE:
		case URL_PATTERN_ALL:
		case URL_PATTERN_UPDATE:
		case URL_PATTERN_UPDATE_SUBMIT:
		case URL_PATTERN_DELETE:
		case WEB_RULE:
		case WEB_RULE_ADD_OR_UPDATE:
		case WEB_RULE_ADD_OR_UPDATE_SUBMIT:
		case WEB_RULE_DELETE:
			m_patternConfigProcessor.processPatternConfig(action, payload, model);
			break;

		case TOPOLOGY_GRAPH_NODE_CONFIG_LIST:
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE:
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE_SUBMIT:
		case TOPOLOGY_GRAPH_NODE_CONFIG_DELETE:
		case TOPOLOGY_GRAPH_EDGE_CONFIG_LIST:
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE:
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT:
		case TOPOLOGY_GRAPH_EDGE_CONFIG_DELETE:
		case TOPOLOGY_GRAPH_PRODUCT_LINE:
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE:
		case TOPOLOGY_GRAPH_PRODUCT_LINE_DELETE:
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE_SUBMIT:
			m_topologyConfigProcessor.process(action, payload, model);
			break;

		case METRIC_CONFIG_ADD_OR_UPDATE:
		case METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT:
		case METRIC_RULE_ADD_OR_UPDATE:
		case METRIC_RULE_ADD_OR_UPDATE_SUBMIT:
		case METRIC_CONFIG_LIST:
		case METRIC_CONFIG_DELETE:
		case METRIC_RULE_CONFIG_UPDATE:
			m_metricConfigProcessor.process(action, payload, model);
			break;

		case EXCEPTION:
		case EXCEPTION_THRESHOLD_DELETE:
		case EXCEPTION_THRESHOLD_UPDATE:
		case EXCEPTION_THRESHOLD_ADD:
		case EXCEPTION_THRESHOLD_UPDATE_SUBMIT:
		case EXCEPTION_EXCLUDE_DELETE:
		case EXCEPTION_EXCLUDE_UPDATE:
		case EXCEPTION_EXCLUDE_ADD:
		case EXCEPTION_EXCLUDE_UPDATE_SUBMIT:
			m_exceptionConfigProcessor.process(action, payload, model);
			break;

		case NETWORK_RULE_CONFIG_LIST:
		case NETWORK_RULE_ADD_OR_UPDATE:
		case NETWORK_RULE_ADD_OR_UPDATE_SUBMIT:
		case NETWORK_RULE_DELETE:
		case NET_GRAPH_CONFIG_UPDATE:
			m_networkConfigProcessor.process(action, payload, model);
			break;

		case SYSTEM_RULE_CONFIG_LIST:
		case SYSTEM_RULE_ADD_OR_UPDATE:
		case SYSTEM_RULE_ADD_OR_UPDATE_SUBMIT:
		case SYSTEM_RULE_DELETE:
			m_systemConfigProcessor.process(action, payload, model);
			break;

		case HEARTBEAT_RULE_CONFIG_LIST:
		case HEARTBEAT_RULE_ADD_OR_UPDATE:
		case HEARTBEAT_RULE_ADD_OR_UPDATE_SUBMIT:
		case HEARTBEAT_RULE_DELETE:
			m_heartbeatConfigProcessor.process(action, payload, model);
			break;

		case APP_LIST:
		case APP_UPDATE:
		case APP_SUBMIT:
		case APP_PAGE_DELETE:
		case APP_CONFIG_UPDATE:
		case APP_CONFIG_FETCH:
		case APP_RULE:
		case APP_RULE_ADD_OR_UPDATE:
		case APP_RULE_ADD_OR_UPDATE_SUBMIT:
		case APP_RULE_DELETE:
		case APP_COMPARISON_CONFIG_UPDATE:
			m_appConfigProcessor.process(action, payload, model);
			break;

		case ALERT_DEFAULT_RECEIVERS:
		case ALERT_POLICY:
			m_alertConfigProcessor.process(action, payload, model);
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	public void store(String userName, String accountName, Payload payload) {
		ConfigModification modification = m_configModificationDao.createLocal();

		modification.setUserName(userName);
		modification.setAccountName(accountName);
		modification.setActionName(payload.getAction().getName());
		modification.setDate(new Date());
		modification.setArgument(new JsonBuilder().toJson(payload));

		try {
			m_configModificationDao.insert(modification);
		} catch (Exception ex) {
			Cat.logError(ex);
		}
	}

	private void storeModifyInfo(Context ctx, Payload payload) {
		Cookie cookie = ctx.getCookie("ct");
		Action action = payload.getAction();
		String lowName = action.getName().toLowerCase();

		if (lowName.indexOf("update") >= 0 || lowName.indexOf("submit") >= 0 || lowName.indexOf("delete") >= 0) {
			if (cookie != null) {
				String cookieValue = cookie.getValue();

				try {
					String[] values = cookieValue.split("\\|");
					String userName = values[0];
					String account = values[1];

					if (userName.startsWith("\"")) {
						userName = userName.substring(1, userName.length() - 1);
					}
					userName = URLDecoder.decode(userName, "UTF-8");

					store(userName, account, payload);
				} catch (Exception ex) {
					Cat.logError("store cookie fail:" + cookieValue, new RuntimeException());
				}
			} else {
				Cat.logError("cannot get cookie info", new RuntimeException());
			}
		}
	}

}
