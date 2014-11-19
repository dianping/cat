package com.dianping.cat.status;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.model.entity.Property;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.transform.BaseVisitor;

public class StatusInfoCollector extends BaseVisitor {
	private MessageStatistics m_statistics;

	private boolean m_dumpLocked;

	private String m_jars;

	private String m_dataPath = "/data";

	public StatusInfoCollector(MessageStatistics statistics, String jars) {
		m_statistics = statistics;
		m_jars = jars;
	}

	@SuppressWarnings("unchecked")
	private void appendPropertyMap(Property property, Map<String, Property> map) {
		List<Map<String, Property>> properties;
		properties = (List<Map<String, Property>>) property.getValue();

		if (properties == null) {
			properties = new ArrayList<Map<String, Property>>();

			property.setValue(properties);
		}

		properties.add(map);
	}

	private int countThreadsByPrefix(ThreadInfo[] threads, String... prefixes) {
		int count = 0;

		for (ThreadInfo thread : threads) {
			for (String prefix : prefixes) {
				if (thread.getThreadName().startsWith(prefix)) {
					count++;
				}
			}
		}

		return count;
	}

	private int countThreadsBySubstring(ThreadInfo[] threads, String... substrings) {
		int count = 0;

		for (ThreadInfo thread : threads) {
			for (String str : substrings) {
				if (thread.getThreadName().contains(str)) {
					count++;
				}
			}
		}

		return count;
	}

	private String getThreadDump(ThreadInfo[] threads) {
		StringBuilder sb = new StringBuilder(32768);
		int index = 1;

		TreeMap<String, ThreadInfo> sortedThreads = new TreeMap<String, ThreadInfo>();

		for (ThreadInfo thread : threads) {
			sortedThreads.put(thread.getThreadName(), thread);
		}

		for (ThreadInfo thread : sortedThreads.values()) {
			sb.append(index++).append(": ").append(thread);
		}

		return sb.toString();
	}

	boolean isInstanceOfInterface(Class<?> clazz, String interfaceName) {
		if (clazz == Object.class) {
			return false;
		} else if (clazz.getName().equals(interfaceName)) {
			return true;
		}

		Class<?>[] interfaceclasses = clazz.getInterfaces();

		for (Class<?> interfaceClass : interfaceclasses) {
			if (isInstanceOfInterface(interfaceClass, interfaceName)) {
				return true;
			}
		}

		return isInstanceOfInterface(clazz.getSuperclass(), interfaceName);
	}

	private void setDiskInfo(StatusInfo status) {
		Property diskVolume = status.findOrCreateProperty("DiskVolume");
		File[] roots = File.listRoots();

		if (roots != null) {
			for (File root : roots) {
				Map<String, Property> diskRootMap = new HashMap<String, Property>();

				diskRootMap.put("Name", new Property("Name").setValue(root.getAbsolutePath()));
				appendPropertyMap(diskVolume, diskRootMap);
			}
		}

		File data = new File(m_dataPath);

		if (data.exists()) {
			Map<String, Property> diskDataMap = new HashMap<String, Property>();

			diskDataMap.put("Name", new Property("Name").setValue(data.getAbsolutePath()));
			appendPropertyMap(diskVolume, diskDataMap);
		}
	}

	public StatusInfoCollector setDumpLocked(boolean dumpLocked) {
		m_dumpLocked = dumpLocked;
		return this;
	}

	private void setMemoryInfo(StatusInfo status) {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		Runtime runtime = Runtime.getRuntime();

		status.findOrCreateProperty("Max").setValue(runtime.maxMemory());
		status.findOrCreateProperty("Total").setValue(runtime.totalMemory());
		status.findOrCreateProperty("Free").setValue(runtime.freeMemory());
		status.findOrCreateProperty("HeapUsage").setValue(memoryBean.getHeapMemoryUsage().getUsed());
		status.findOrCreateProperty("NonHeapUsage").setValue(memoryBean.getNonHeapMemoryUsage().getUsed());

		for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
			if (mpBean.getName().contains("Eden")) {
				status.findOrCreateProperty("EdenUsage").setValue(mpBean.getUsage().getUsed());
			} else if (mpBean.getName().contains("Survivor")) {
				status.findOrCreateProperty("SurvivorUsage").setValue(mpBean.getUsage().getUsed());
			}
		}

		List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();

		for (GarbageCollectorMXBean mxbean : beans) {
			if (mxbean.isValid()) {
				Property gc = status.findOrCreateProperty("GC");
				Map<String, Property> gcMap = new HashMap<String, Property>();

				gcMap.put("Name", new Property("Name").setValue(mxbean.getName()));
				gcMap.put("Count", new Property("Count").setValue(mxbean.getCollectionCount()));
				gcMap.put("Time", new Property("Time").setValue(mxbean.getCollectionTime()));

				appendPropertyMap(gc, gcMap);
			}
		}
	}

	private void setMessageInfo(StatusInfo status) {
		if (m_statistics != null) {
			status.findOrCreateProperty("Produced").setValue(m_statistics.getProduced());
			status.findOrCreateProperty("Overflowed").setValue(m_statistics.getOverflowed());
			status.findOrCreateProperty("Bytes").setValue(m_statistics.getBytes());
		}
	}

	private void setOsInfo(StatusInfo status) {
		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

		status.findOrCreateProperty("Arch").setValue(bean.getArch());
		status.findOrCreateProperty("Name").setValue(bean.getName());
		status.findOrCreateProperty("Version").setValue(bean.getVersion());
		status.findOrCreateProperty("AvailableProcessors").setValue(bean.getAvailableProcessors());
		status.findOrCreateProperty("SystemLoadAverage").setValue(bean.getSystemLoadAverage());

		// for Sun JDK
		if (isInstanceOfInterface(bean.getClass(), "com.sun.management.OperatingSystemMXBean")) {
			com.sun.management.OperatingSystemMXBean b = (com.sun.management.OperatingSystemMXBean) bean;

			status.findOrCreateProperty("TotalPhysicalMemory").setValue(b.getTotalPhysicalMemorySize());
			status.findOrCreateProperty("FreePhysicalMemory").setValue(b.getFreePhysicalMemorySize());
			status.findOrCreateProperty("TotalSwapSpace").setValue(b.getTotalSwapSpaceSize());
			status.findOrCreateProperty("FreeSwapSpace").setValue(b.getFreeSwapSpaceSize());
			status.findOrCreateProperty("ProcessTime").setValue(b.getProcessCpuTime());
			status.findOrCreateProperty("CommittedVirtualMemory").setValue(b.getCommittedVirtualMemorySize());
		}
	}

	private void setRuntimeInfo(StatusInfo status) {
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

		status.findOrCreateProperty("StartTime").setValue(runtimeBean.getStartTime());
		status.findOrCreateProperty("UpTime").setValue(runtimeBean.getUptime());
		status.findOrCreateProperty("JavaClasspath").setValue(m_jars);
		status.findOrCreateProperty("JavaVersion").setValue(System.getProperty("java.version"));
		status.findOrCreateProperty("UserDir").setValue(System.getProperty("user.dir"));
		status.findOrCreateProperty("UserName").setValue(System.getProperty("user.name"));
	}

	private void setThreadInfo(StatusInfo status) {
		ThreadInfo[] threads;
		ThreadMXBean threadbean = ManagementFactory.getThreadMXBean();

		threadbean.setThreadContentionMonitoringEnabled(true);

		if (m_dumpLocked) {
			threads = threadbean.dumpAllThreads(true, true);
		} else {
			threads = threadbean.dumpAllThreads(false, false);
		}

		status.findOrCreateProperty("Count").setValue(threadbean.getThreadCount());
		status.findOrCreateProperty("DaemonCount").setValue(threadbean.getDaemonThreadCount());
		status.findOrCreateProperty("PeekCount").setValue(threadbean.getPeakThreadCount());
		status.findOrCreateProperty("TotalStartedCount").setValue((int) threadbean.getTotalStartedThreadCount());
		status.findOrCreateProperty("CatThreadCount").setValue(countThreadsByPrefix(threads, "Cat-"));
		status.findOrCreateProperty("PigeonThreadCount").setValue(
		      countThreadsByPrefix(threads, "Pigeon-", "DPSF-", "Netty-", "Client-ResponseProcessor"));

		int jbossThreadsCount = countThreadsByPrefix(threads, "http-", "catalina-exec-");
		int jettyThreadsCount = countThreadsBySubstring(threads, "@qtp");

		status.findOrCreateProperty("HttpThreadCount").setValue(jbossThreadsCount + jettyThreadsCount);
		status.findOrCreateProperty("Dump").setValue(getThreadDump(threads));
	}

	@Override
	public void visitStatus(StatusInfo status) {
		status.setTimestamp(new Date());

		setOsInfo(status);
		setDiskInfo(status);
		setRuntimeInfo(status);
		setMemoryInfo(status);
		setThreadInfo(status);
		setMessageInfo(status);

		super.visitStatus(status);
	}

}