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
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dal.report.ConfigModification;
import com.dianping.cat.home.dal.report.ConfigModificationDao;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.config.processor.AlertConfigProcessor;
import com.dianping.cat.system.page.config.processor.AppConfigProcessor;
import com.dianping.cat.system.page.config.processor.DatabaseConfigProcessor;
import com.dianping.cat.system.page.config.processor.DisplayConfigProcessor;
import com.dianping.cat.system.page.config.processor.EventConfigProcessor;
import com.dianping.cat.system.page.config.processor.ExceptionConfigProcessor;
import com.dianping.cat.system.page.config.processor.GlobalConfigProcessor;
import com.dianping.cat.system.page.config.processor.HeartbeatConfigProcessor;
import com.dianping.cat.system.page.config.processor.MetricConfigProcessor;
import com.dianping.cat.system.page.config.processor.NetworkConfigProcessor;
import com.dianping.cat.system.page.config.processor.WebConfigProcessor;
import com.dianping.cat.system.page.config.processor.StorageConfigProcessor;
import com.dianping.cat.system.page.config.processor.SystemConfigProcessor;
import com.dianping.cat.system.page.config.processor.ThirdPartyConfigProcessor;
import com.dianping.cat.system.page.config.processor.TopologyConfigProcessor;
import com.dianping.cat.system.page.config.processor.TransactionConfigProcessor;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private GlobalConfigProcessor m_globalConfigProcessor;

	@Inject
	private ThirdPartyConfigProcessor m_thirdPartyConfigProcessor;

	@Inject
	private WebConfigProcessor m_patternConfigProcessor;

	@Inject
	private TopologyConfigProcessor m_topologyConfigProcessor;

	@Inject
	private MetricConfigProcessor m_metricConfigProcessor;

	@Inject
	private ExceptionConfigProcessor m_exceptionConfigProcessor;

	@Inject
	private NetworkConfigProcessor m_networkConfigProcessor;

	@Inject
	private DatabaseConfigProcessor m_databaseConfigProcessor;

	@Inject
	private SystemConfigProcessor m_systemConfigProcessor;

	@Inject
	private HeartbeatConfigProcessor m_heartbeatConfigProcessor;

	@Inject
	private AppConfigProcessor m_appConfigProcessor;

	@Inject
	private AlertConfigProcessor m_alertConfigProcessor;

	@Inject
	private TransactionConfigProcessor m_transactionConfigProcessor;

	@Inject
	private EventConfigProcessor m_eventConfigProcessor;

	@Inject
	private StorageConfigProcessor m_storageConfigProcessor;

	@Inject
	private DisplayConfigProcessor m_displayConfigProfessor;

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
	@PreInboundActionMeta("login")
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
		case PROJECT_UPDATE_SUBMIT:
		case PROJECT_DELETE:
		case DOMAIN_GROUP_CONFIGS:
		case DOMAIN_GROUP_CONFIG_UPDATE:
		case DOMAIN_GROUP_CONFIG_DELETE:
		case DOMAIN_GROUP_CONFIG_SUBMIT:
		case BUG_CONFIG_UPDATE:
		case ROUTER_CONFIG_UPDATE:
		case ALERT_SENDER_CONFIG_UPDATE:
		case BLACK_CONFIG_UPDATE:
		case STORAGE_GROUP_CONFIG_UPDATE:
		case SERVER_FILTER_CONFIG_UPDATE:
		case ALL_REPORT_CONFIG:
			m_globalConfigProcessor.process(action, payload, model);
			break;

		case THIRD_PARTY_RULE_CONFIGS:
		case THIRD_PARTY_RULE_UPDATE:
		case THIRD_PARTY_RULE_SUBMIT:
		case THIRD_PARTY_RULE_DELETE:
			m_thirdPartyConfigProcessor.process(action, payload, model);
			break;

		case AGGREGATION_ALL:
		case AGGREGATION_UPDATE:
		case AGGREGATION_UPDATE_SUBMIT:
		case AGGREGATION_DELETE:
		case URL_PATTERN_CONFIG_UPDATE:
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
		case TOPO_GRAPH_FORMAT_CONFIG_UPDATE:
			m_topologyConfigProcessor.process(action, payload, model);
			break;

		case METRIC_CONFIG_ADD_OR_UPDATE:
		case METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT:
		case METRIC_RULE_ADD_OR_UPDATE:
		case METRIC_RULE_ADD_OR_UPDATE_SUBMIT:
		case METRIC_CONFIG_LIST:
		case METRIC_CONFIG_DELETE:
		case METRIC_CONFIG_BATCH_DELETE:
		case METRIC_RULE_CONFIG_UPDATE:
			m_metricConfigProcessor.process(action, payload, model);
			break;

		case EXCEPTION:
		case EXCEPTION_THRESHOLD_DELETE:
		case EXCEPTION_THRESHOLD_UPDATE:
		case EXCEPTION_THRESHOLD_ADD:
		case EXCEPTION_THRESHOLD_UPDATE_SUBMIT:
		case EXCEPTION_EXCLUDE_DELETE:
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

		case DATABASE_RULE_CONFIG_LIST:
		case DATABASE_RULE_ADD_OR_UPDATE:
		case DATABASE_RULE_ADD_OR_UPDATE_SUBMIT:
		case DATABASE_RULE_DELETE:
			m_databaseConfigProcessor.process(action, payload, model);
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

		case STORAGE_RULE:
		case STORAGE_RULE_ADD_OR_UPDATE:
		case STORAGE_RULE_ADD_OR_UPDATE_SUBMIT:
		case STORAGE_RULE_DELETE:
			m_storageConfigProcessor.process(action, payload, model);
			break;

		case APP_NAME_CHECK:
		case APP_LIST:
		case APP_COMMMAND_UPDATE:
		case APP_COMMAND_SUBMIT:
		case APP_COMMAND_DELETE:
		case APP_CODE_UPDATE:
		case APP_CODE_SUBMIT:
		case APP_CODE_ADD:
		case APP_CODE_DELETE:
		case APP_SPEED_UPDATE:
		case APP_SPEED_ADD:
		case APP_SPEED_DELETE:
		case APP_SPEED_SUBMIT:
		case APP_CONFIG_UPDATE:
		case APP_RULE:
		case APP_RULE_ADD_OR_UPDATE:
		case APP_RULE_ADD_OR_UPDATE_SUBMIT:
		case APP_RULE_DELETE:
		case APP_COMPARISON_CONFIG_UPDATE:
		case APP_RULE_BATCH_UPDATE:
		case APP_CONSTANT_ADD:
		case APP_CONSTANT_UPDATE:
		case APP_CONSTATN_DELETE:
		case APP_CONSTATN_SUBMIT:
		case APP_COMMAND_FORMAT_CONFIG:
			m_appConfigProcessor.process(action, payload, model);
			break;

		case TRANSACTION_RULE:
		case TRANSACTION_RULE_ADD_OR_UPDATE:
		case TRANSACTION_RULE_ADD_OR_UPDATE_SUBMIT:
		case TRANSACTION_RULE_DELETE:
			m_transactionConfigProcessor.process(action, payload, model);
			break;

		case EVENT_RULE:
		case EVENT_RULE_ADD_OR_UPDATE:
		case EVENT_RULE_ADD_OR_UPDATE_SUBMIT:
		case EVENT_RULE_DELETE:
			m_eventConfigProcessor.process(action, payload, model);
			break;

		case ALERT_DEFAULT_RECEIVERS:
		case ALERT_POLICY:
			m_alertConfigProcessor.process(action, payload, model);
			break;

		case DISPLAY_POLICY:
		case ACTIVITY_CONFIG_UPDATE:
			m_displayConfigProfessor.process(action, payload, model);
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
		}
	}

}
