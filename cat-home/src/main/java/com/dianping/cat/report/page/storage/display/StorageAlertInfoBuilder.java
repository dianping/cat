/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.page.storage.display;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.Alert;
import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.alarm.spi.AlertLevel;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.storage.alert.entity.Detail;
import com.dianping.cat.home.storage.alert.entity.Machine;
import com.dianping.cat.home.storage.alert.entity.Operation;
import com.dianping.cat.home.storage.alert.entity.Storage;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.home.storage.alert.entity.Target;
import com.dianping.cat.report.page.storage.StorageConstants;

@Named
public class StorageAlertInfoBuilder {

	@Inject
	private AlertService m_alertService;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	public int buildLevel(int level, int other) {
		return level > other ? level : other;
	}

	public Map<String, StorageAlertInfo> buildStorageAlertInfos(Date start, Date end, int minuteCounts, String type,
							List<Alert> alerts) {
		Map<String, StorageAlertInfo> results = prepareBlankAlert(start.getTime(), end.getTime(), minuteCounts, type);

		for (Alert alert : alerts) {
			long time = alert.getAlertTime().getTime();
			long current = time - time % TimeHelper.ONE_MINUTE - TimeHelper.ONE_MINUTE;
			Date date = new Date(current);
			StorageAlertInfo alertInfo = results.get(m_sdf.format(date));

			if (alertInfo != null) {
				parseAlertEntity(alert, alertInfo);
			} else {
				Cat.logError(new RuntimeException("Error date in alert: " + alert.toString() + ", alert date: " + date));
			}
		}
		return results;
	}

	public StorageAlertInfo makeAlertInfo(String id, Date start) {
		StorageAlertInfo alertInfo = new StorageAlertInfo(id);

		alertInfo.setStartTime(start);
		alertInfo.setEndTime(new Date(start.getTime() + TimeHelper.ONE_MINUTE - 1));
		return alertInfo;
	}

	public void parseAlertEntity(Alert alert, StorageAlertInfo alertInfo) {
		String name = alert.getDomain();
		List<String> fields = Splitters.by(";").split(alert.getMetric());
		String ip = fields.get(0);
		String operation = fields.get(1);
		String target = queryTargetTitle(fields.get(2));
		AlertLevel alertLevel = AlertLevel.findByName(alert.getType());
		int level = alertLevel.getPriority();
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

	private Map<String, StorageAlertInfo> prepareBlankAlert(long start, long end, int minuteCounts, String type) {
		Map<String, StorageAlertInfo> results = new LinkedHashMap<String, StorageAlertInfo>();

		for (long s = start; s <= end; s += TimeHelper.ONE_MINUTE) {
			String title = m_sdf.format(new Date(s));
			StorageAlertInfo blankAlertInfo = makeAlertInfo(type, new Date(start));

			results.put(title, blankAlertInfo);
		}
		return results;
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
