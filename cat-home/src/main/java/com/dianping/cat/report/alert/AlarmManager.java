package com.dianping.cat.report.alert;

import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.alert.transaction.TransactionAlert;

@Named
public class AlarmManager extends ContainerHolder {

	public void startAlarm() {
//		Map<String, ServerAlarm> serverAlarms = lookupMap(ServerAlarm.class);
//
//		for (ServerAlarm serverAlarm : serverAlarms.values()) {
//			Threads.forGroup("cat").start(serverAlarm);
//		}

		BusinessAlert businessAlert = lookup(BusinessAlert.class);
		ExceptionAlert exceptionAlert = lookup(ExceptionAlert.class);
		HeartbeatAlert heartbeatAlert = lookup(HeartbeatAlert.class);
		ThirdPartyAlert thirdPartyAlert = lookup(ThirdPartyAlert.class);
		ThirdPartyAlertBuilder alertBuildingTask = lookup(ThirdPartyAlertBuilder.class);
		TransactionAlert transactionAlert = lookup(TransactionAlert.class);
		EventAlert eventAlert = lookup(EventAlert.class);

//		StorageSQLAlert storageDatabaseAlert = lookup(StorageSQLAlert.class);
//		StorageCacheAlert storageCacheAlert = lookup(StorageCacheAlert.class);
//		StorageRPCAlert storageRpcAlert = lookup(StorageRPCAlert.class);
		
		//AppAlert appAlert = lookup(AppAlert.class);
		//JsAlert jsAlert = lookup(JsAlert.class);
		//AjaxAlert ajaxAlert = lookup(AjaxAlert.class);
		//CrashAlert crashAlert = lookup(CrashAlert.class);

		Threads.forGroup("cat").start(businessAlert);
		Threads.forGroup("cat").start(exceptionAlert);
		Threads.forGroup("cat").start(heartbeatAlert);
		Threads.forGroup("cat").start(thirdPartyAlert);
		Threads.forGroup("cat").start(alertBuildingTask);
		Threads.forGroup("cat").start(transactionAlert);
		Threads.forGroup("cat").start(eventAlert);

//		Threads.forGroup("cat").start(storageDatabaseAlert);
//		Threads.forGroup("cat").start(storageCacheAlert);
//		Threads.forGroup("cat").start(storageRpcAlert);
		
		//Threads.forGroup("cat").start(appAlert);
		//Threads.forGroup("cat").start(jsAlert);
		//Threads.forGroup("cat").start(ajaxAlert);
		//Threads.forGroup("cat").start(crashAlert);
	}
}
