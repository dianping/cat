package com.dianping.cat.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Uptime;

public class StateTask extends AbstractTask {

	public static final String CATALINA_PATH = "/data/applogs/tomcat/catalina.out";

	public static final String MD5_PATH = "/usr/sbin/sshd";

	private static String m_md5String;

	private static String m_hostName;

	public StateTask(Configuration config) {
		super(config);

		m_hostName = tellHostName();
		m_domain = config.getStateDomain();

		try {
			m_md5String = readFileContent(MD5_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long current = System.currentTimeMillis();

			buildUptimeInfo();
			buildHostIpAddInfo();
			buildCatalinaLogInfo();
			buildTomcatLiveInfo();
			buildHostNameInfo();
			buildSshdInfo();

			long gap = System.currentTimeMillis() - current;

			tellGap(gap, m_domain);

			try {
				if (gap < STATE_MONITOR_PERIOD) {
					Thread.sleep(STATE_MONITOR_PERIOD - gap);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	public void buildUptimeInfo() {
		try {
			Sigar sigar = new Sigar();
			Uptime uptime = sigar.getUptime();
			double time = uptime.getUptime() / 60;
			String url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_uptime_" + m_ipAddr, "avg",
			      String.valueOf(time));

			sendMetric(url);
			
			String url2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_uptime_" + m_ipAddr2, "avg",
			      String.valueOf(time + 10000));

			sendMetric(url2);
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}

	public void buildCatalinaLogInfo() {
		File logFile = new File(CATALINA_PATH);
		if (logFile.exists()) {
			double bytes = logFile.length();
			double kilobytes = (bytes / 1024);
			String url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "jvm_catalinaLogSize_" + m_ipAddr, "sum",
			      String.valueOf(kilobytes));

			sendMetric(url);
			// System.out.println("/data/applogs/tomcat/catalina.out size(KB): " + kilobytes);
		}
	}

	public void buildTomcatLiveInfo() {
		try {
			String pid = searchPidOfTomcat();
			String url;

			if (pid == null) {
				url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "jvm_tomcatLive_" + m_ipAddr, "avg", String.valueOf(0));
			} else {
				url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "jvm_tomcatLive_" + m_ipAddr, "avg", String.valueOf(1));
			}
			sendMetric(url);
		} catch (Throwable e) {
			e.printStackTrace();
		}
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
		return null;
	}

	public void buildSshdInfo() {
		try {
			String currMd5String = readFileContent(MD5_PATH);
			if (m_md5String == null) {
				return;
			}
			String url;
			if (m_md5String.equals(currMd5String)) {
				url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_md5Change_" + m_ipAddr, "avg", String.valueOf(1));
			} else {
				url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_md5Change_" + m_ipAddr, "avg", String.valueOf(0));
			}
			sendMetric(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String tellHostName() {
		String hostname = "";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception exc) {
			try {
				Sigar sigar = new Sigar();
				hostname = sigar.getNetInfo().getHostName();
			} catch (SigarException e) {
				hostname = "localhost.unknown";
			}
		}
		return hostname;
	}

	public void buildHostNameInfo() {
		String hostName = tellHostName();
		String url;
		if (m_hostName.equals(hostName)) {
			url = String
			      .format(m_catUrl, System.currentTimeMillis(), m_domain, "system_hostNameChange_" + m_ipAddr, "avg", String.valueOf(1));
		} else {
			url = String
			      .format(m_catUrl, System.currentTimeMillis(), m_domain, "system_hostNameChange_" + m_ipAddr, "avg", String.valueOf(0));
		}
		sendMetric(url);
	}

	public void buildHostIpAddInfo() {
		String hostIpAdd = tellHostIpAddr();
		String url;
		
		if (m_ipAddr.equals(hostIpAdd)) {
			url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_hostIpChange_" + m_ipAddr, "avg", String.valueOf(1));
		} else {
			url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_hostIpChange_" + m_ipAddr, "avg", String.valueOf(0));
		}
		sendMetric(url);
	}

}
