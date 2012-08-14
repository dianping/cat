package com.dianping.cat.report.project;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.hadoop.dal.Hostinfo;
import com.dianping.cat.hadoop.dal.HostinfoDao;
import com.dianping.cat.hadoop.dal.HostinfoEntity;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class ProjectInfoCache {

	private static final String UNKNOWN = "UnknownProject";

	@Inject
	private HostinfoDao m_hostInfoDao;

	public String getProjectNameFromIp(String ip) {
		if (StringUtils.isEmpty(ip)) {
			return UNKNOWN;
		}
		if (ip.indexOf(':') > 0) {
			ip = ip.substring(0, ip.indexOf(':'));
		}
		try {
			Hostinfo hostInfo = m_hostInfoDao.findByIp(ip, HostinfoEntity.READSET_FULL);
			if (hostInfo != null) {
				return hostInfo.getDomain();
			}
		} catch (DalException e) {
			return UNKNOWN;
		}
		return UNKNOWN;
	}

	public boolean isInTheProject(String ip, String projectName) {
		if (StringUtils.isEmpty(ip)) {
			return false;
		}
		if (ip.indexOf(':') > 0) {
			ip = ip.substring(0, ip.indexOf(':'));
		}
		try {
			Hostinfo hostInfo = m_hostInfoDao.findByIp(ip, HostinfoEntity.READSET_FULL);
			if (hostInfo != null) {
				return true;
			}
		} catch (DalException e) {
			return false;
		}
		return true;
	}
}
