package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.home.dal.alarm.AlarmRuleDao;
import com.dianping.cat.home.dal.alarm.AlarmTemplateDao;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.home.dal.alarm.ScheduledReportDao;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.DailyReportService;
import com.dianping.cat.system.alarm.AlarmContentBuilder;
import com.dianping.cat.system.alarm.AlarmRuleCreator;
import com.dianping.cat.system.alarm.AlarmTask;
import com.dianping.cat.system.alarm.alert.AlertManager;
import com.dianping.cat.system.alarm.connector.Connector;
import com.dianping.cat.system.alarm.connector.impl.ThresholdConnector;
import com.dianping.cat.system.alarm.threshold.ThresholdRuleManager;
import com.dianping.cat.system.alarm.threshold.listener.ExceptionDataListener;
import com.dianping.cat.system.alarm.threshold.listener.ServiceDataListener;
import com.dianping.cat.system.alarm.threshold.listener.ThresholdAlertListener;
import com.dianping.cat.system.event.DefaultEventDispatcher;
import com.dianping.cat.system.event.DefaultEventListenerRegistry;
import com.dianping.cat.system.event.EventDispatcher;
import com.dianping.cat.system.event.EventListenerRegistry;
import com.dianping.cat.system.notify.ReportRender;
import com.dianping.cat.system.notify.ReportRenderImpl;
import com.dianping.cat.system.notify.ScheduledMailTask;
import com.dianping.cat.system.page.alarm.RuleManager;
import com.dianping.cat.system.page.alarm.ScheduledManager;
import com.dianping.cat.system.tool.MailSMS;
import com.dianping.cat.system.tool.MailSMSImpl;

class AlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(AlarmContentBuilder.class));

		all.add(C(AlarmRuleCreator.class)//
		      .req(AlarmRuleDao.class, AlarmTemplateDao.class, ScheduledReportDao.class)//
		      .req(ModelService.class, "event"));

		all.add(C(MailSMS.class, MailSMSImpl.class).req(ServerConfigManager.class));

		all.add(C(ReportRender.class, ReportRenderImpl.class));

		all.add(C(ScheduledMailTask.class).//
		      req(ReportRender.class, MailSMS.class)//
		      .req(DailyReportService.class, ScheduledManager.class)//
		      .req(MailRecordDao.class));

		all.add(C(EventListenerRegistry.class, DefaultEventListenerRegistry.class));

		all.add(C(EventDispatcher.class, DefaultEventDispatcher.class)//
		      .req(EventListenerRegistry.class));

		all.add(C(Connector.class, ThresholdConnector.class));

		all.add(C(AlertManager.class).//
		      req(MailRecordDao.class, MailSMS.class, ServerConfigManager.class));

		all.add(C(ThresholdRuleManager.class).//
		      req(AlarmTemplateDao.class, AlarmRuleDao.class, ServerConfigManager.class));

		all.add(C(ExceptionDataListener.class).//
		      req(EventDispatcher.class, ThresholdRuleManager.class));

		all.add(C(ServiceDataListener.class).//
		      req(EventDispatcher.class, ThresholdRuleManager.class));

		all.add(C(ThresholdAlertListener.class).//
		      req(AlertManager.class, RuleManager.class, AlarmContentBuilder.class));

		all.add(C(AlarmTask.class).//
		      req(EventDispatcher.class, Connector.class, ThresholdRuleManager.class));

		return all;
	}
}
