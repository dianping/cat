package com.dianping.cat.report.page.storage.topology;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.alert.report.storage.entity.Attribute;
import com.dianping.cat.home.alert.report.storage.entity.Detail;
import com.dianping.cat.home.alert.report.storage.entity.Machine;
import com.dianping.cat.home.alert.report.storage.entity.Operation;
import com.dianping.cat.home.alert.report.storage.entity.Segment;
import com.dianping.cat.home.alert.report.storage.entity.Storage;
import com.dianping.cat.home.alert.report.storage.entity.StorageAlertReport;
import com.dianping.cat.report.alert.AlertLevel;
import com.dianping.cat.report.alert.sender.AlertEntity;
import com.dianping.cat.report.alert.storage.AbstractStorageAlert.ReportFetcherParam;

public class StorageGraphBuilder {

	@Inject
	private StorageAlertInfoManager m_manager;

	private int queryLevel(String level) {
		if (AlertLevel.ERROR.equals(level)) {
			return 2;
		} else if (AlertLevel.WARNING.equals(level)) {
			return 1;
		} else {
			return 0;
		}
	}

	private int buildLevel(int level, int other) {
		return level > other ? level : other;
	}

	public void processAlertEntity(int minute, AlertEntity entity, ReportFetcherParam param) {
		int level = queryLevel(entity.getLevel());
		String name = param.getName();
		String ip = param.getMachine();
		String opertaion = param.getMethod();
		String attribute = param.getAttribute();
		Segment segment = getReport().findOrCreateSegment(minute);

		Storage storage = segment.findOrCreateStorage(name);
		storage.setLevel(buildLevel(storage.getLevel(), level));

		Machine machine = storage.findOrCreateMachine(ip);
		machine.setLevel(buildLevel(machine.getLevel(), level));

		Operation op = machine.findOrCreateOperation(opertaion);
		op.setLevel(buildLevel(op.getLevel(), level));

		Attribute at = op.findOrCreateAttribute(attribute);
		at.setLevel(buildLevel(at.getLevel(), level));
		at.getDetails().add(new Detail(entity.getContent()).setLevel(level));
	}

	private StorageAlertReport getReport() {
		return m_manager.findOrCreate(TimeHelper.getCurrentHour().getTime());
	}

}
