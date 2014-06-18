package com.dianping.cat.agent.monitor.executors.system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Uptime;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.executors.AbstractExecutor;
import com.dianping.cat.agent.monitor.executors.DataEntity;

public class SystemStateExecutor extends AbstractExecutor implements Initializable {

	public static final String ID = "SystemStateExecutor";

	private static final String MD5_PATH = "/usr/sbin/sshd";

	private Sigar m_sigar = new Sigar();

	private String m_md5String;

	private String m_hostName;

	private String m_ipAddr;

	@Override
	public void initialize() throws InitializationException {
		m_hostName = tellHostName();
		m_ipAddr = m_environmentConfig.getIp();

		try {
			m_md5String = readFileContent(MD5_PATH);
		} catch (IOException e) {
			Cat.logError(e);
		}
	}

	public String tellHostName() {
		String hostname = "";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception exc) {
			try {
				hostname = m_sigar.getNetInfo().getHostName();
			} catch (SigarException e) {
				Cat.logError(e);
			}
		}
		return hostname;
	}

	@Override
	public List<DataEntity> execute() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		entities.addAll(buildUptimeInfo());
		entities.addAll(buildHostIpAddInfo());
		entities.addAll(buildHostNameInfo());
		entities.addAll(buildSshdInfo());

		return entities;
	}

	public String readFileContent(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
				line = br.readLine();
			}
			return sb.toString();
		} catch (Exception e) {
			Cat.logError(e);
		} finally {
			br.close();
		}
		return null;
	}

	public List<DataEntity> buildUptimeInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			Uptime uptime = m_sigar.getUptime();
			double time = uptime.getUptime() / 60;

			DataEntity entity = new DataEntity();
			entity.setId(buildSystemDataEntityId("uptime")).setType(AVG_TYPE).setTime(System.currentTimeMillis())
			      .setValue(time);
			addGroupDomainInfo(entity);
			entities.add(entity);
		} catch (SigarException e) {
			Cat.logError(e);
		}
		return entities;
	}

	public boolean hostIpAddrChanged() {
		try {
			String ifNames[] = m_sigar.getNetInterfaceList();

			for (int i = 0; i < ifNames.length; i++) {
				String name = ifNames[i];
				NetInterfaceConfig ifconfig = m_sigar.getNetInterfaceConfig(name);
				String currentIp = ifconfig.getAddress();

				if (currentIp.equals(m_ipAddr)) {
					return false;
				}
			}
		} catch (SigarException e) {
			Cat.logError(e);
		}
		return true;
	}

	public List<DataEntity> buildHostIpAddInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		DataEntity entity = new DataEntity();

		entity.setId(buildSystemDataEntityId("hostIpChange")).setType(AVG_TYPE).setTime(System.currentTimeMillis());
		addGroupDomainInfo(entity);
		
		if (!hostIpAddrChanged()) {
			entity.setValue(1);
		} else {
			entity.setValue(0);
		}
		entities.add(entity);
		return entities;
	}

	public List<DataEntity> buildHostNameInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		DataEntity entity = new DataEntity();
		String hostName = tellHostName();

		entity.setId(buildSystemDataEntityId("hostNameChange")).setType(AVG_TYPE).setTime(System.currentTimeMillis());
		addGroupDomainInfo(entity);
		
		if (m_hostName.equals(hostName)) {
			entity.setValue(1);
		} else {
			entity.setValue(0);
		}
		entities.add(entity);
		return entities;
	}

	public List<DataEntity> buildSshdInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			String currMd5String = readFileContent(MD5_PATH);
			DataEntity entity = new DataEntity();

			entity.setId(buildSystemDataEntityId("md5Change")).setType(AVG_TYPE).setTime(System.currentTimeMillis());
			addGroupDomainInfo(entity);
			
			if (m_md5String != null && m_md5String.equals(currMd5String)) {
				entity.setValue(1);
			} else {
				entity.setValue(0);
			}
			entities.add(entity);
		} catch (IOException e) {
			Cat.logError(e);
		}
		return entities;
	}

	@Override
	public String getId() {
		return ID;
	}
}
