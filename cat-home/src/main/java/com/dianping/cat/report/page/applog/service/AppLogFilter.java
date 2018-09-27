package com.dianping.cat.report.page.applog.service;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;

import com.dianping.cat.app.crash.AppLog;
import com.dianping.cat.config.LogLevel;

public class AppLogFilter {

	private List<String> m_appVersions;

	private List<String> m_platformVersions;

	private List<String> m_levels;

	private List<String> m_devices;

	public AppLogFilter(String query) {
		if (StringUtils.isNotEmpty(query)) {
			List<String> querys = Splitters.by(";").split(query);

			if (querys.size() == 4) {
				m_appVersions = Splitters.by(":").noEmptyItem().split(querys.get(0));
				m_platformVersions = Splitters.by(":").noEmptyItem().split(querys.get(1));
				m_levels = Splitters.by(":").noEmptyItem().split(querys.get(2));
				m_devices = Splitters.by(":").noEmptyItem().split(querys.get(3));
			}
		}
	}

	public boolean checkFlag(AppLog log) {
		return checkFlag(m_appVersions, log.getAppVersion()) && checkFlag(m_platformVersions, log.getPlatformVersion())
		      && checkFlag(m_devices, log.getDeviceBrand() + "-" + log.getDeviceModel())
		      && checkFlag(m_levels, LogLevel.getName(log.getLevel()));
	}

	private boolean checkFlag(List<String> myFields, String field) {
		if (myFields == null || myFields.isEmpty() || !myFields.isEmpty() && myFields.contains(field)) {
			return true;
		} else {
			return false;
		}
	}

}
