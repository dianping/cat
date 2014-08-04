package com.dianping.cat.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.dal.HostinfoDao;
import com.dianping.cat.core.dal.HostinfoEntity;

public class HostinfoService implements Initializable {

	@Inject
	private HostinfoDao m_hostinfoDao;

	private Map<String, Hostinfo> m_hostinfos = new ConcurrentHashMap<String, Hostinfo>();

	public Hostinfo createLocal() {
		return m_hostinfoDao.createLocal();
	}

	public boolean deleteHostinfo(Hostinfo host) {
		int id = host.getId();
		Iterator<Entry<String, Hostinfo>> iterator = m_hostinfos.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, Hostinfo> entry = iterator.next();
			Hostinfo hostinfo = entry.getValue();
			if (hostinfo.getId() == id) {
				iterator.remove();

				try {
					hostinfo.setKeyId(hostinfo.getId());
					m_hostinfoDao.deleteByPK(hostinfo);
					return true;
				} catch (Exception e) {
					Cat.logError("delete hostinfo error " + hostinfo.toString(), e);
					return false;
				}
			}
		}

		return true;
	}

	public List<Hostinfo> findAll() throws DalException {
		return new ArrayList<Hostinfo>(m_hostinfos.values());
	}

	public Hostinfo findByIp(String ip) {
		return m_hostinfos.get(ip);
	}

	public Hostinfo findHostinfo(int id) {
		Iterator<Entry<String, Hostinfo>> iterator = m_hostinfos.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, Hostinfo> entry = iterator.next();
			Hostinfo hostinfo = entry.getValue();
			if (hostinfo.getId() == id) {
				return hostinfo;
			}
		}

		return new Hostinfo();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			List<Hostinfo> hostinfos = m_hostinfoDao.findAllIp(HostinfoEntity.READSET_FULL);
			for (Hostinfo hostinfo : hostinfos) {
				m_hostinfos.put(hostinfo.getIp(), hostinfo);
			}
		} catch (DalException e) {
			Cat.logError("initialize HostService error", e);
		}
	}

	public boolean insert(Hostinfo hostinfo) throws DalException {
		m_hostinfos.put(hostinfo.getIp(), hostinfo);

		int result = m_hostinfoDao.insert(hostinfo);
		if (result == 1) {
			return true;
		} else {
			return false;
		}
	}

	public boolean updateHostinfo(Hostinfo hostinfo) {
		m_hostinfos.put(hostinfo.getIp(), hostinfo);

		try {
			m_hostinfoDao.updateByPK(hostinfo, HostinfoEntity.UPDATESET_FULL);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}
}
