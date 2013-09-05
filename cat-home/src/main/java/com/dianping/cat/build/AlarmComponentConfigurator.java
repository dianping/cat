package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.system.notify.ReportRender;
import com.dianping.cat.system.notify.ReportRenderImpl;
import com.dianping.cat.system.notify.ScheduledMailTask;
import com.dianping.cat.system.page.alarm.ScheduledManager;
import com.dianping.cat.system.tool.MailSMS;
import com.dianping.cat.system.tool.MailSMSImpl;

class AlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MailSMS.class, MailSMSImpl.class).req(ServerConfigManager.class));

		all.add(C(ReportRender.class, ReportRenderImpl.class));

		all.add(C(ScheduledMailTask.class).//
		      req(ReportRender.class, MailSMS.class)//
		      .req(ReportService.class, ScheduledManager.class)//
		      .req(MailRecordDao.class));

		return all;
	}
}
