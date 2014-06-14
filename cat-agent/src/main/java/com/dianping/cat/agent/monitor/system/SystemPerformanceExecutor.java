package com.dianping.cat.agent.monitor.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.AbstractExecutor;
import com.dianping.cat.agent.monitor.DataEntity;

public class SystemPerformanceExecutor extends AbstractExecutor {

	public static final String ID = "PerformanceExecutor";

	private static final String ETH_NAME = "eth0";

	private static final List<String> DISK_LIST = new ArrayList<String>(Arrays.asList("/data", "/usr", "/var"));

	private Sigar m_sigar = new Sigar();

	private Cpu m_preCpu;

	private Cpu m_curCpu;

	private NetInterfaceStat m_preIfStat;

	private NetInterfaceStat m_curIfStat;

	@Override
	public List<DataEntity> execute() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		entities.addAll(buildCpuInfo());
		entities.addAll(buildDiskUsage());
		entities.addAll(buildFlowInfo());
		entities.addAll(buildSwapInfo());
		entities.addAll(buildLoadInfo());

		return entities;
	}

	public List<DataEntity> buildCpuInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			double cpuUsage = 0.0;

			if (m_preCpu != null) {
				m_curCpu = m_sigar.getCpu();
				long totalIdle = m_curCpu.getIdle() - m_preCpu.getIdle();
				long totalTime = m_curCpu.getTotal() - m_preCpu.getTotal();

				if (totalIdle > 0 && totalTime > 0) {
					cpuUsage = 1 - 1.0 * totalIdle / totalTime;
					m_preCpu = m_curCpu;
				}
				DataEntity entity = new DataEntity();

				entity.setId(buildSystemDataEntityId("cpu")).setType(AVG_TYPE).setTime(System.currentTimeMillis())
				      .setValue(cpuUsage);
				entities.add(entity);
			} else {
				m_preCpu = m_sigar.getCpu();
			}
		} catch (SigarException e) {
			Cat.logError(e);
		}
		return entities;
	}

	public List<DataEntity> buildDiskUsage() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			FileSystem[] fileSystems = m_sigar.getFileSystemList();
			long current = System.currentTimeMillis();

			for (FileSystem fs : fileSystems) {
				if (fs.getType() == FileSystem.TYPE_LOCAL_DISK && DISK_LIST.contains(fs.getDirName())) {
					FileSystemUsage usage = m_sigar.getFileSystemUsage(fs.getDirName());
					DataEntity entity = new DataEntity();

					entity.setId(buildSystemDataEntityId(fs.getDirName() + "-usage")).setType(AVG_TYPE).setTime(current)
					      .setValue(usage.getUsePercent());
					entities.add(entity);
				}
			}
		} catch (SigarException e) {
			Cat.logError(e);
		}
		return entities;
	}

	public List<DataEntity> buildFlowInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			if (m_preIfStat != null) {
				m_curIfStat = m_sigar.getNetInterfaceStat(ETH_NAME);
				long totalRxBytes = m_curIfStat.getRxBytes() - m_preIfStat.getRxBytes();
				long totalTxBytes = m_curIfStat.getTxBytes() - m_preIfStat.getTxBytes();
				m_preIfStat = m_curIfStat;

				long current = System.currentTimeMillis();
				DataEntity inFlow = new DataEntity();

				inFlow.setId(buildSystemDataEntityId(ETH_NAME + "-in-flow")).setType(SUM_TYPE).setTime(current)
				      .setValue(totalRxBytes);
				entities.add(inFlow);

				DataEntity outFlow = new DataEntity();
				outFlow.setId(buildSystemDataEntityId(ETH_NAME + "-out-flow")).setType(SUM_TYPE).setTime(current)
				      .setValue(totalTxBytes);
				entities.add(outFlow);
			} else {
				m_preIfStat = m_sigar.getNetInterfaceStat(ETH_NAME);
			}
		} catch (SigarException e) {
			Cat.logError(e);
		}
		return entities;
	}

	public List<DataEntity> buildSwapInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			Swap curSwap = m_sigar.getSwap();
			double swapUsage = 1.0 * curSwap.getUsed() / curSwap.getTotal();
			DataEntity entity = new DataEntity();

			entity.setId(buildSystemDataEntityId("swap")).setType(AVG_TYPE).setTime(System.currentTimeMillis())
			      .setValue(swapUsage);
			entities.add(entity);
		} catch (SigarException e) {
			Cat.logError(e);
		}
		return entities;
	}

	public ArrayList<DataEntity> buildLoadInfo() {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			double[] loadAverages = m_sigar.getLoadAverage();
			DataEntity entity = new DataEntity();

			entity.setId(buildSystemDataEntityId("load")).setType(AVG_TYPE).setTime(System.currentTimeMillis())
			      .setValue(loadAverages[0]);
			entities.add(entity);
		} catch (SigarException e) {
			Cat.logError(e);
		}
		return entities;
	}

	@Override
	public String getId() {
		return ID;
	}

}
