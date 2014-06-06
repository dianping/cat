package com.dianping.cat.agent.systemAgent;

import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

public class PerformanceTask extends AbstractTask {

	private Sigar m_sigar;

	private Cpu m_preCpu;

	private Cpu m_curCpu;

	private NetInterfaceStat m_preIfStat;

	private NetInterfaceStat m_curIfStat;

	private String m_domain;

	private String m_ethName;

	public PerformanceTask(Configuration config) {
		super(config);

		m_sigar = new Sigar();
		m_domain = m_config.getPerformanceDomain();
		m_ethName = m_config.getEthName();
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long current = System.currentTimeMillis();

			buildCpuInfo();
			buildDiskUsage();
			buildEth0Info();
			buildSwapInfo();
			buildLoadInfo();

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

	public void buildCpuInfo() {
		try {
			double cpuUsage = 0.0;

			if (m_preCpu == null) {
				m_preCpu = m_sigar.getCpu();
				return;
			}

			m_curCpu = m_sigar.getCpu();
			long totalIdle = m_curCpu.getIdle() - m_preCpu.getIdle();
			long totalTime = m_curCpu.getTotal() - m_preCpu.getTotal();

			if (totalIdle > 0 && totalTime > 0) {
				cpuUsage = 1 - 1.0 * totalIdle / totalTime;
				m_preCpu = m_curCpu;
			}
			String url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_cpu_" + m_ipAddr, "avg",
			      String.valueOf(cpuUsage));
			sendMetric(url);
			
			String url2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_cpu_" + m_ipAddr2, "avg",
			      String.valueOf(cpuUsage + 0.2));
			sendMetric(url2);
		} catch (SigarException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void buildDiskUsage() {
		try {
			FileSystem[] fileSystems = m_sigar.getFileSystemList();

			for (FileSystem fs : fileSystems) {
				if (fs.getType() == FileSystem.TYPE_LOCAL_DISK) {
					FileSystemUsage usage = m_sigar.getFileSystemUsage(fs.getDirName());
					String url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_" + fs.getDirName() + "_usage_" + m_ipAddr,
					      "avg", String.valueOf(usage.getUsePercent()));

					sendMetric(url);
					
					String url2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_" + fs.getDirName() + "_usage_" + m_ipAddr2,
					      "avg", String.valueOf(usage.getUsePercent() + 0.2));

					sendMetric(url2);
				}
			}
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}

	public void buildEth0Info() {
		try {
			if (m_preIfStat == null) {
				m_preIfStat = m_sigar.getNetInterfaceStat(m_ethName);

				return;
			}
			m_curIfStat = m_sigar.getNetInterfaceStat(m_ethName);
			long totalRxBytes = m_curIfStat.getRxBytes() - m_preIfStat.getRxBytes();
			long totalTxBytes = m_curIfStat.getTxBytes() - m_preIfStat.getTxBytes();
			m_preIfStat = m_curIfStat;
			String urlIn = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_" + m_ethName + "-in-flow_" + m_ipAddr, "sum",
			      String.valueOf(totalRxBytes));

			sendMetric(urlIn);

			String urlOut = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_" + m_ethName + "-out-flow_" + m_ipAddr, "sum",
			      String.valueOf(totalTxBytes));

			sendMetric(urlOut);
			
			String urlIn2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_" + m_ethName + "-in-flow_" + m_ipAddr2, "sum",
			      String.valueOf(totalRxBytes + 100000));

			sendMetric(urlIn2);

			String urlOut2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_" + m_ethName + "-out-flow_" + m_ipAddr2, "sum",
			      String.valueOf(totalTxBytes + 100000));

			sendMetric(urlOut2);
			
			
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}

	public void buildSwapInfo() {
		try {
			Swap curSwap = m_sigar.getSwap();
			double swapUsage = 1.0 * curSwap.getUsed() / curSwap.getTotal();
			String url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_swap_" + m_ipAddr, "avg",
			      String.valueOf(swapUsage));

			sendMetric(url);
			
			String url2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_swap_" + m_ipAddr2, "avg",
			      String.valueOf(swapUsage + 0.2));

			sendMetric(url2);
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}

	public void buildLoadInfo() {
		try {
			double[] loadAverages = m_sigar.getLoadAverage();
			String url = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_load_" + m_ipAddr, "avg",
			      String.valueOf(loadAverages[0]));

			sendMetric(url);
			
			String url2 = String.format(m_catUrl, System.currentTimeMillis(), m_domain, "system_load_" + m_ipAddr2, "avg",
			      String.valueOf(loadAverages[0] + 2));

			sendMetric(url2);
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}
}
