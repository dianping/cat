package com.dianping.cat.system.page.alarm;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dainping.cat.home.dal.user.DpAdminLogin;
import com.dianping.cat.system.SystemPage;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.unidal.web.mvc.annotation.PreInboundActionMeta;

public class Handler implements PageHandler<Context> {
	public static final String FAIL = "Fail";

	public static final String SUCCESS = "Success";

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private RecordManager m_recordManager;

	@Inject
	private RuleManager m_ruleManager;

	@Inject
	private ScheduledManager m_scheduledManager;

	@Inject
	private TemplateManager m_templateManager;

	private int getLoginUserId(Context ctx) {
		DpAdminLogin member = ctx.getSigninMember();
		return member.getLoginId();
	}

	@Override
	@PreInboundActionMeta("login")
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "alarm")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "alarm")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		int userId = getLoginUserId(ctx);
		boolean result = false;

		switch (action) {
		case ALARM_RECORD_LIST:
			m_recordManager.queryUserAlarmRecords(model, userId);
			break;
		case ALARM_RULE_ADD:
			m_ruleManager.ruleAdd(payload, model);
			break;
		case ALARM_RULE_ADD_SUBMIT:
			m_ruleManager.ruleAddSubmit(payload, model);
			break;
		case ALARM_RULE_UPDATE:
			m_ruleManager.ruleUpdate(payload, model);
			break;
		case ALARM_RULE_UPDATE_SUBMIT:
			m_ruleManager.ruleUpdateSubmit(payload, model);
			break;
		case EXCEPTION_ALARM_RULE_DELETE:
			m_ruleManager.ruleDelete(payload);
			m_ruleManager.queryExceptionRuleList(model, userId);
			break;
		case EXCEPTION_ALARM_RULE_SUB:
			result = m_ruleManager.ruleSub(payload, userId);
			if (result) {
				model.setOpState(SUCCESS);
			} else {
				model.setOpState(FAIL);
			}
			break;
		case EXCEPTION_ALARM_RULE_LIST:
			m_ruleManager.queryExceptionRuleList(model, userId);
			break;
		case ALARM_TEMPLATE_LIST:
			m_templateManager.queryTemplateByName(payload, model);
			break;
		case ALARM_TEMPLATE_ADD:
			break;
		case ALARM_TEMPLATE_ADD_SUBMIT:
			m_templateManager.templateAddSubmit(payload, model);
			break;
		case ALARM_TEMPLATE_DELETE:
			break;
		case ALARM_TEMPLATE_UPDATE:
			m_templateManager.templateUpdate(payload, model);
			break;
		case ALARM_TEMPLATE_UPDATE_SUBMIT:
			m_templateManager.templateUpdateSubmit(payload, model);
			break;
		case SERVICE_ALARM_RULE_DELETE:
			m_ruleManager.ruleDelete(payload);
			m_ruleManager.queryServiceRuleList(model, userId);
			break;
		case SERVICE_ALARM_RULE_LIST:
			m_ruleManager.queryServiceRuleList(model, userId);
			break;
		case SERVICE_ALARM_RULE_SUB:
			result = m_ruleManager.ruleSub(payload, userId);
			if (result) {
				model.setOpState(SUCCESS);
			} else {
				model.setOpState(FAIL);
			}
			break;
		case ALARM_RECORD_DETAIL:
			m_recordManager.queryAlarmRecordDetail(payload, model);
			break;
		case SCHEDULED_REPORT_ADD:
			m_scheduledManager.scheduledReportAdd(payload, model);
			break;
		case SCHEDULED_REPORT_ADD_SUBMIT:
			m_scheduledManager.scheduledReportAddSubmit(payload, model);
			break;
		case SCHEDULED_REPORT_DELETE:
			m_scheduledManager.scheduledReportDelete(payload);
			m_scheduledManager.queryScheduledReports(model, userId);
			break;
		case SCHEDULED_REPORT_LIST:
			m_scheduledManager.queryScheduledReports(model, userId);
			break;
		case SCHEDULED_REPORT_UPDATE:
			m_scheduledManager.scheduledReportUpdate(payload, model);
			break;
		case SCHEDULED_REPORT_UPDATE_SUBMIT:
			m_scheduledManager.scheduledReportUpdateSubmit(payload, model);
			break;
		case SCHEDULED_REPORT_SUB:
			result = m_scheduledManager.scheduledReportSub(payload, userId);
			if (result) {
				model.setOpState(SUCCESS);
			} else {
				model.setOpState(FAIL);
			}
			break;
		case REPORT_RECORD_LIST:
			m_recordManager.queryUserReportRecords(model, userId);
			break;
		}

		model.setAction(payload.getAction());
		model.setPage(SystemPage.ALARM);
		m_jspViewer.view(ctx, model);
	}

}
