package com.dianping.cat.report.page.storage.topology;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.storage.alert.entity.Detail;
import com.dianping.cat.home.storage.alert.entity.Machine;
import com.dianping.cat.home.storage.alert.entity.Operation;
import com.dianping.cat.home.storage.alert.entity.Storage;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.home.storage.alert.entity.Target;
import com.dianping.cat.report.alert.AlertLevel;
import com.dianping.cat.report.alert.sender.AlertEntity;
import com.dianping.cat.report.alert.storage.AbstractStorageAlert.ReportFetcherParam;
import com.dianping.cat.report.page.storage.StorageConstants;

public class StorageAlertInfoBuilder {

	@Inject
	private StorageAlertInfoRTContainer m_container;

	public int buildLevel(int level, int other) {
		return level > other ? level : other;
	}

	public StorageAlertInfo getAlertInfo(String type, int minute) {
		long current = TimeHelper.getCurrentHour().getTime() + minute * TimeHelper.ONE_MINUTE;

		return m_container.findOrCreate(type, current);
	}

	public Map<Long, StorageAlertInfo> buildStorageAlertInfos(List<Alert> alerts) {
		Map<Long, StorageAlertInfo> alertInfos = new LinkedHashMap<Long, StorageAlertInfo>();

		for (Alert alert : alerts) {
			long time = alert.getAlertTime().getTime();
			long current = time - time % TimeHelper.ONE_MINUTE - TimeHelper.ONE_MINUTE;
			StorageAlertInfo alertInfo = alertInfos.get(current);

			if (alertInfo == null) {
				alertInfo = m_container.makeAlertInfo(alert.getCategory(), new Date(current));

				alertInfos.put(current, alertInfo);
			}
			parseAlertEntity(alert, alertInfo);
		}
		return alertInfos;
	}

	public void parseAlertEntity(Alert alert, StorageAlertInfo alertInfo) {
		String name = alert.getDomain();
		List<String> fields = Splitters.by(";").split(alert.getMetric());
		String ip = fields.get(0);
		String operation = fields.get(1);
		String target = queryTargetTitle(fields.get(2));
		int level = queryLevel(alert.getType());

		Storage storage = alertInfo.findOrCreateStorage(name);
		storage.incCount();
		storage.setLevel(buildLevel(storage.getLevel(), level));

		Machine machine = storage.findOrCreateMachine(ip);
		machine.incCount();
		machine.setLevel(buildLevel(machine.getLevel(), level));

		Operation op = machine.findOrCreateOperation(operation);
		op.incCount();
		op.setLevel(buildLevel(op.getLevel(), level));

		Target tg = op.findOrCreateTarget(target);
		tg.incCount();
		tg.setLevel(buildLevel(tg.getLevel(), level));
		tg.getDetails().add(new Detail(alert.getContent()).setLevel(level));
	}

	public void processAlertEntity(String type, int minute, AlertEntity entity, ReportFetcherParam param) {
		int level = queryLevel(entity.getLevel());
		String name = param.getName();
		String ip = param.getMachine();
		String opertaion = param.getMethod();
		String target = queryTargetTitle(param.getTarget());

		Storage storage = getAlertInfo(type, minute).findOrCreateStorage(name);
		storage.incCount();
		storage.setLevel(buildLevel(storage.getLevel(), level));

		Machine machine = storage.findOrCreateMachine(ip);
		machine.incCount();
		machine.setLevel(buildLevel(machine.getLevel(), level));

		Operation op = machine.findOrCreateOperation(opertaion);
		op.incCount();
		op.setLevel(buildLevel(op.getLevel(), level));

		Target tg = op.findOrCreateTarget(target);
		tg.incCount();
		tg.setLevel(buildLevel(tg.getLevel(), level));
		tg.getDetails().add(new Detail(entity.getContent()).setLevel(level));
	}

	private int queryLevel(String level) {
		if (AlertLevel.ERROR.equals(level)) {
			return 2;
		} else if (AlertLevel.WARNING.equals(level)) {
			return 1;
		} else {
			return 0;
		}
	}

	private String queryTargetTitle(String target) {
		if (StorageConstants.AVG.equals(target)) {
			return "响应时间";
		} else if (StorageConstants.ERROR.equals(target)) {
			return "错误数";
		} else if (StorageConstants.ERROR_PERCENT.equals(target)) {
			return "错误率";
		} else {
			return target;
		}
	}
}
