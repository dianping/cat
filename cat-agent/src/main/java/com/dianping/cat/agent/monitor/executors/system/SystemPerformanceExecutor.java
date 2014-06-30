package com.dianping.cat.agent.monitor.executors.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.NetStat;
import org.hyperic.sigar.ProcStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;
import org.hyperic.sigar.Who;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.DataEntity;
import com.dianping.cat.agent.monitor.executors.AbstractExecutor;

public class SystemPerformanceExecutor extends AbstractExecutor {

	public static final String ID = "PerformanceExecutor";

	private Sigar m_sigar = new Sigar();

	private Map<String, NetInterfaceStat> m_preIfStatMap = new HashMap<String, NetInterfaceStat>();

	private Map<String, FileSystemUsage> m_fileSystemUsageMap = new HashMap<String, FileSystemUsage>();

	private List<DataEntity> buildCpuInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			CpuPerc cpuPerc = m_sigar.getCpuPerc();
			double system = cpuPerc.getSys();
			double iowait = cpuPerc.getWait();
			double nice = cpuPerc.getNice();
			double steal = cpuPerc.getStolen();
			double user = cpuPerc.getUser();
			double softirq = cpuPerc.getSoftIrq();
			double idle = cpuPerc.getIdle();
			double irq = cpuPerc.getIrq();
			Map<String, Double> values = new HashMap<String, Double>();

			values.put(buildSystemId("sysCpu"), system);
			values.put(buildSystemId("iowaitCpu"), iowait);
			values.put(buildSystemId("niceCpu"), nice);
			values.put(buildSystemId("stealCpu"), steal);
			values.put(buildSystemId("userCpu"), user);
			values.put(buildSystemId("softirqCpu"), softirq);
			values.put(buildSystemId("idleCpu"), idle);
			values.put(buildSystemId("irqCpu"), irq);
			entities.addAll(buildEntities(values, AVG_TYPE));

		} catch (Exception e) {
			Cat.logError(e);
		}
		return entities;
	}

	private List<DataEntity> buildDiskUsage() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			FileSystem[] fileSystems = m_sigar.getFileSystemList();

			for (FileSystem fs : fileSystems) {
				String dirName = fs.getDirName();

				if (fs.getType() == FileSystem.TYPE_LOCAL_DISK && m_envConfig.getDiskList().contains(dirName)) {
					Map<String, Double> values = new HashMap<String, Double>();
					FileSystemUsage usage = m_sigar.getFileSystemUsage(dirName);
					double usedPerc = usage.getUsePercent();
					double inodePerc = 1.0 * usage.getFreeFiles() / usage.getFiles();

					values.put(buildSystemId(dirName + "-usage"), usedPerc);
					values.put(buildSystemId(dirName + "-freeInodes"), inodePerc);
					entities.addAll(buildEntities(values, AVG_TYPE));

					FileSystemUsage preUsage = m_fileSystemUsageMap.get(dirName);

					if (preUsage != null) {
						double read = usage.getDiskReadBytes() - preUsage.getDiskReadBytes();
						double write = usage.getDiskWriteBytes() - preUsage.getDiskWriteBytes();

						values.clear();
						values.put(buildSystemId(dirName + "-read"), read);
						values.put(buildSystemId(dirName + "-write"), write);
						entities.addAll(buildEntities(values, SUM_TYPE));
					}
					m_fileSystemUsageMap.put(dirName, usage);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return entities;
	}

	private ArrayList<DataEntity> buildLoadInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			double[] loadAvgs = m_sigar.getLoadAverage();
			double loadAvg1 = loadAvgs[0];
			double loadAvg5 = loadAvgs[1];

			Map<String, Double> values = new HashMap<String, Double>();

			values.put(buildSystemId("loadAvg1"), loadAvg1);
			values.put(buildSystemId("loadAvg5"), loadAvg5);
			entities.addAll(buildEntities(values, AVG_TYPE));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return entities;
	}

	private List<DataEntity> buildMemInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			List<String> lines = m_commandUtils.runShell("free");
			Iterator<String> iterator = lines.iterator();

			if (lines.size() >= 2) {
				iterator.next();
				String line = iterator.next();
				String[] outputs = line.split(" +");
				double total = Double.parseDouble(outputs[1]);
				double used = Double.parseDouble(outputs[2]) / total;
				double free = Double.parseDouble(outputs[3]) / total;
				double shared = Double.parseDouble(outputs[4]) / total;
				double buffers = Double.parseDouble(outputs[5]) / total;
				double cached = Double.parseDouble(outputs[6]) / total;
				Map<String, Double> values = new HashMap<String, Double>();

				values.put(buildSystemId("totalMem"), total);
				values.put(buildSystemId("usedMem"), used);
				values.put(buildSystemId("freeMem"), free);
				values.put(buildSystemId("sharedMem"), shared);
				values.put(buildSystemId("buffersMem"), buffers);
				values.put(buildSystemId("cachedMem"), cached);
				entities.addAll(buildEntities(values, AVG_TYPE));
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return entities;
	}

	private List<DataEntity> buildNetworkInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		for (String netInterface : m_envConfig.getTrafficInterfaceList()) {
			try {
				NetInterfaceStat curIfStat = m_sigar.getNetInterfaceStat(netInterface);
				NetInterfaceStat preIfStat = m_preIfStatMap.get(netInterface);

				if (preIfStat != null) {
					Map<String, Double> values = new HashMap<String, Double>();
					double totalRxBytes = curIfStat.getRxBytes() - preIfStat.getRxBytes();
					double totalTxBytes = curIfStat.getTxBytes() - preIfStat.getTxBytes();

					values.put(buildSystemId(netInterface + "-inFlow"), totalRxBytes);
					values.put(buildSystemId(netInterface + "-outFlow"), totalTxBytes);

					if (m_envConfig.getPackageInterface().equals(netInterface)) {
						double txDropped = curIfStat.getTxDropped() - preIfStat.getTxDropped();
						double txErrors = curIfStat.getTxErrors() - preIfStat.getTxErrors();
						double txCollisions = curIfStat.getTxCollisions() - preIfStat.getTxCollisions();

						values.put(buildSystemId(netInterface + "-dropped"), txDropped);
						values.put(buildSystemId(netInterface + "-errors"), txErrors);
						values.put(buildSystemId(netInterface + "-collisions"), txCollisions);
					}
					entities.addAll(buildEntities(values, SUM_TYPE));
				}
				m_preIfStatMap.put(netInterface, curIfStat);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return entities;
	}

	private List<DataEntity> buildProcessInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			ProcStat procStat = m_sigar.getProcStat();
			double totalProc = procStat.getTotal();
			double totalRunning = procStat.getRunning();
			Map<String, Double> values = new HashMap<String, Double>();

			values.put(buildSystemId("totalProcess"), totalProc);
			values.put(buildSystemId("runningProcess"), totalRunning);
			entities.addAll(buildEntities(values, AVG_TYPE));
		} catch (SigarException e) {
			Cat.logError(e);
		}
		return entities;
	}

	private List<DataEntity> buildSwapInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			Swap curSwap = m_sigar.getSwap();
			double totalSwap = curSwap.getTotal();
			double swapUsage = totalSwap > 0.0 ? curSwap.getFree() / totalSwap : 0.0;
			Map<String, Double> values = new HashMap<String, Double>();

			values.put(buildSystemId("swapUsage"), swapUsage);
			entities.addAll(buildEntities(values, AVG_TYPE));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return entities;
	}

	private List<DataEntity> buildTcpConnectionInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();
		try {
			NetStat netStat = m_sigar.getNetStat();
			double tcpCon = netStat.getTcpEstablished();
			Map<String, Double> values = new HashMap<String, Double>();

			values.put(buildSystemId("establishedTcp"), tcpCon);
			entities.addAll(buildEntities(values, AVG_TYPE));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return entities;
	}

	private List<DataEntity> buildUserNumber() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			Who[] whos = m_sigar.getWhoList();
			Set<String> users = new HashSet<String>();

			for (Who user : whos) {
				users.add(user.getUser());
			}
			double number = users.size();
			Map<String, Double> values = new HashMap<String, Double>();

			values.put(buildSystemId("loginUsers"), number);
			entities.addAll(buildEntities(values, AVG_TYPE));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return entities;
	}

	@Override
	public List<DataEntity> execute() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		entities.addAll(buildCpuInfo());
		entities.addAll(buildDiskUsage());
		entities.addAll(buildNetworkInfo());
		entities.addAll(buildSwapInfo());
		entities.addAll(buildLoadInfo());
		entities.addAll(buildTcpConnectionInfo());
		entities.addAll(buildUserNumber());
		entities.addAll(buildProcessInfo());
		entities.addAll(buildMemInfo());

		return entities;
	}

	@Override
	public String getId() {
		return ID;
	}

}
