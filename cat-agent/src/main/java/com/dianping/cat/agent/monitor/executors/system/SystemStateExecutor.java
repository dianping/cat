package com.dianping.cat.agent.monitor.executors.system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.Uptime;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.DataEntity;
import com.dianping.cat.agent.monitor.executors.AbstractExecutor;

public class SystemStateExecutor extends AbstractExecutor implements Initializable {

	public static final String ID = "SystemStateExecutor";

	private Sigar m_sigar = new Sigar();

	private String m_md5String;

	private String m_hostName;

	private String m_ipAddr;

	public List<DataEntity> buildHostIpAddInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		double value = 0;

		if (!hostIpAddrChanged()) {
			value = 1;
		}
		Map<String, Double> values = new HashMap<String, Double>();

		values.put(buildSystemId("hostIpChange"), value);
		entities.addAll(buildEntities(values, AVG_TYPE));
		return entities;
	}

	public List<DataEntity> buildHostNameInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		String hostName = fetchHostName();
		double value = 0;

		if (m_hostName.equals(hostName)) {
			value = 1;
		}
		Map<String, Double> values = new HashMap<String, Double>();

		values.put(buildSystemId("hostNameChange"), value);
		entities.addAll(buildEntities(values, AVG_TYPE));
		return entities;
	}

	public List<DataEntity> buildSshdInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			String currMd5String = readFileContent(m_envConfig.getMd5Path());
			double value = 0;

			if (m_md5String.equals(currMd5String)) {
				value = 1;
			}
			Map<String, Double> values = new HashMap<String, Double>();

			values.put(buildSystemId("md5Change"), value);
			entities.addAll(buildEntities(values, AVG_TYPE));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return entities;
	}

	public List<DataEntity> buildUptimeInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			Uptime uptime = m_sigar.getUptime();
			double time = uptime.getUptime() / 60;
			Map<String, Double> values = new HashMap<String, Double>();

			values.put(buildSystemId("uptime"), time);
			entities.addAll(buildEntities(values, AVG_TYPE));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return entities;
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

	public String fetchHostName() {
		String hostname = "";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception exc) {
			try {
				hostname = m_sigar.getNetInfo().getHostName();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return hostname;
	}

	@Override
	public String getId() {
		return ID;
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
		} catch (Exception e) {
			Cat.logError(e);
		}
		return true;
	}

	@Override
	public void initialize() throws InitializationException {
		m_hostName = fetchHostName();
		m_ipAddr = m_envConfig.getIp();

		try {
			m_md5String = readFileContent(m_envConfig.getMd5Path());
		} catch (IOException e) {
			Cat.logError(e);
		}
	}

	public String readFileContent(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line).append(System.getProperty("line.separator"));
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
}
