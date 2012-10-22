package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.home.dal.alarm.AlarmRuleDao;
import com.dianping.cat.home.dal.alarm.AlarmTemplateDao;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.home.dal.alarm.ScheduledReportDao;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.DailyReportService;
import com.dianping.cat.system.alarm.DefaultAlarmCreator;
import com.dianping.cat.system.alarm.ExceptionAlarmTask;
import com.dianping.cat.system.alarm.exception.listener.ExceptionAlertListener;
import com.dianping.cat.system.alarm.exception.listener.ExceptionDataListener;
import com.dianping.cat.system.event.DefaultEventDispatcher;
import com.dianping.cat.system.event.DefaultEventListenerRegistry;
import com.dianping.cat.system.event.EventDispatcher;
import com.dianping.cat.system.event.EventListenerRegistry;
import com.dianping.cat.system.notify.ReportRender;
import com.dianping.cat.system.notify.ReportRenderImpl;
import com.dianping.cat.system.notify.ScheduledMailTask;
import com.dianping.cat.system.page.alarm.ScheduledManager;
import com.dianping.cat.system.tool.MailSMS;
import com.dianping.cat.system.tool.MailSMSImpl;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class AlarmComponentConfigurator extends AbstractResourceConfigurator {

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DefaultAlarmCreator.class)//
		      .req(AlarmRuleDao.class, AlarmTemplateDao.class, ScheduledReportDao.class)//
		      .req(ModelService.class, "event"));

		all.add(C(MailSMS.class, MailSMSImpl.class));

		all.add(C(ReportRender.class, ReportRenderImpl.class));

		all.add(C(ScheduledMailTask.class).//
		      req(ReportRender.class, MailSMS.class)//
		      .req(DailyReportService.class, ScheduledManager.class)//
		      .req(MailRecordDao.class));

		all.add(C(EventListenerRegistry.class, DefaultEventListenerRegistry.class));

		all.add(C(EventDispatcher.class, DefaultEventDispatcher.class)//
		      .req(EventListenerRegistry.class));

		all.add(C(ExceptionAlertListener.class).//
		      req(EventDispatcher.class));

		all.add(C(ExceptionDataListener.class).//
		      req(EventDispatcher.class));

		all.add(C(ExceptionAlarmTask.class).//
		      req(EventDispatcher.class));

		return all;
	}
}
