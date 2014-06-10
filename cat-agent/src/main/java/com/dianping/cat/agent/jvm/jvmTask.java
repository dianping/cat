package com.dianping.cat.agent.jvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.dianping.cat.agent.AbstractTask;
import com.dianping.cat.agent.Configuration;

public class jvmTask extends AbstractTask {

	public jvmTask(Configuration config) {
		super(config);
		m_domain = config.getJvmDomain();
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long current = System.currentTimeMillis();

			buildJVMGenInfo();

			long gap = System.currentTimeMillis() - current;
			tellGap(gap, m_domain);

			try {
				if (gap < METRIC_MONITOR_PERIOD) {
					Thread.sleep(METRIC_MONITOR_PERIOD - gap);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	public void buildJVMGenInfo() {
		try {
			String pid = searchPidOfTomcat();

			if (pid == null) {
				System.out.println("No tomcat running");
				return;
			}

			Process process = Runtime.getRuntime().exec("jstat -gcutil " + pid);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			reader.readLine();

			String output = reader.readLine();
			String[] metrics = output.split(" +");
			String edenUsage = String.valueOf(Double.valueOf(metrics[2]) / 100);
			String oldUsage = String.valueOf(Double.valueOf(metrics[3]) / 100);
			String permUsage = String.valueOf(Double.valueOf(metrics[4]) / 100);

			String edenUrl = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "jvm_edenUsage_" + m_ipAddr, "avg",
			      String.valueOf(edenUsage));
			String oldUrl = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "jvm_oldUsage_" + m_ipAddr, "avg",
			      String.valueOf(oldUsage));
			String permUrl = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "jvm_permUsage_" + m_ipAddr, "avg",
			      String.valueOf(permUsage));

			sendMetric(edenUrl);
			sendMetric(oldUrl);
			sendMetric(permUrl);
			
			String edenUrl2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "jvm_edenUsage_" + m_ipAddr2, "avg",
			      String.valueOf(edenUsage + 0.2));
			String oldUrl2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "jvm_oldUsage_" + m_ipAddr2, "avg",
			      String.valueOf(oldUsage + 0.2));
			String permUrl2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "jvm_permUsage_" + m_ipAddr2, "avg",
			      String.valueOf(permUsage + 0.2));

			sendMetric(edenUrl2);
			sendMetric(oldUrl2);
			sendMetric(permUrl2);

			reader.close();
			// System.out.println("edenUsage: " + edenUsage + " oldUsage: " + oldUsage + " permUsage: " + permUsage);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
