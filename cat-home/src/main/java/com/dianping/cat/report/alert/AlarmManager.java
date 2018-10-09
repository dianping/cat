package com.dianping.cat.report.alert;

import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.transaction.TransactionAlert;

@Named
public class AlarmManager extends ContainerHolder {

	public void startAlarm() {
		BusinessAlert businessAlert = lookup(BusinessAlert.class);
		ExceptionAlert exceptionAlert = lookup(ExceptionAlert.class);
		HeartbeatAlert heartbeatAlert = lookup(HeartbeatAlert.class);
		TransactionAlert transactionAlert = lookup(TransactionAlert.class);
		EventAlert eventAlert = lookup(EventAlert.class);

		Threads.forGroup("cat").start(businessAlert);
		Threads.forGroup("cat").start(exceptionAlert);
		Threads.forGroup("cat").start(heartbeatAlert);
		Threads.forGroup("cat").start(transactionAlert);
		Threads.forGroup("cat").start(eventAlert);
	}
}
