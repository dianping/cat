package com.dianping.cat.agent.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class AbstractTask implements Runnable {

	public static final int METRIC_MONITOR_PERIOD = 5 * 1000;

	public static final int STATE_MONITOR_PERIOD = 60 * 1000;

	public String m_catUrl;

	public String m_domain;

	public String m_ipAddr;
	
	public String m_ipAddr2 = "10.10.1.1";
	
	public Configuration m_config;

	public long gapSum;

	public long gapCount;

	public AbstractTask(Configuration config) {
		m_config = config;
		m_catUrl = "http://" + m_config.getCatAddr() + ":2281/cat/r/monitor?timestamp=%1$s&group="
		      + m_config.getGroupName() + "&domain=%2$s&key=%3$s&op=%4$s&%4$s=%5$s";
		m_ipAddr = tellHostIpAddr();
	}

	public String tellHostIpAddr() {
		String ipAddr = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

//		
//		try {
//			Sigar sigar = new Sigar();
//			ipAddr = sigar.getNetInterfaceConfig("en0").getAddress();
//		} catch (SigarException e) {
//			ipAddr = "ipAddress.unknown";
//		}
		return ipAddr;
	}

	public void tellGap(long gap, String task) {
		gapSum += gap;

		gapCount++;
		System.out.println(task + " task gap: " + gap);
		System.out.println(task + " task average gap: " + gapSum / gapCount);
	}

	public String sendMetric(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	public String searchPidOfTomcat() throws Throwable {
		Process process = Runtime.getRuntime().exec(
		      new String[] { "/bin/sh", "-c", "ps aux | grep tomcat | grep -v grep" });
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String output = reader.readLine();

		if (output == null) {
			return null;
		}
		if (reader.readLine() != null) {
			throw new Exception("More than one tomcat is running");
		}
		reader.close();
		String[] outputs = output.split(" +");
		String pid = outputs[1];

		return pid;
	}

	@Override
	public void run() {

	}

}
