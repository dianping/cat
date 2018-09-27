package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;
import com.dianping.cat.alarm.spi.decorator.DecoratorManager;
import com.dianping.cat.alarm.spi.receiver.ContactorManager;
import com.dianping.cat.alarm.spi.rule.DefaultDataChecker;
import com.dianping.cat.alarm.spi.sender.MailSender;
import com.dianping.cat.alarm.spi.sender.Sender;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.alarm.spi.sender.SmsSender;
import com.dianping.cat.alarm.spi.sender.WeixinSender;
import com.dianping.cat.alarm.spi.spliter.DXSpliter;
import com.dianping.cat.alarm.spi.spliter.MailSpliter;
import com.dianping.cat.alarm.spi.spliter.SmsSpliter;
import com.dianping.cat.alarm.spi.spliter.Spliter;
import com.dianping.cat.alarm.spi.spliter.SpliterManager;
import com.dianping.cat.alarm.spi.spliter.WeixinSpliter;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(new CatDatabaseConfigurator().defineComponents());

		all.add(A(SenderConfigManager.class));

		all.add(A(DefaultDataChecker.class));
		all.add(A(DecoratorManager.class));
		all.add(A(ContactorManager.class));

		all.add(A(AlertPolicyManager.class));

		all.add(C(Spliter.class, MailSpliter.ID, MailSpliter.class));

		all.add(C(Spliter.class, SmsSpliter.ID, SmsSpliter.class));

		all.add(C(Spliter.class, WeixinSpliter.ID, WeixinSpliter.class));

		all.add(C(Spliter.class, DXSpliter.ID, DXSpliter.class));

		all.add(A(SpliterManager.class));

		all.add(C(Sender.class, MailSender.ID, MailSender.class).req(SenderConfigManager.class));

		all.add(C(Sender.class, SmsSender.ID, SmsSender.class).req(SenderConfigManager.class));

		all.add(C(Sender.class, WeixinSender.ID, WeixinSender.class).req(SenderConfigManager.class));

		all.add(A(SenderManager.class));

		all.add(A(AlertManager.class));

		all.add(A(AlertService.class));

		all.add(A(AlertConfigManager.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
