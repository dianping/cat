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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.model.entity.Extension;
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

	private void appendExtension(Property property, Map<String, String> map) {
		Extension e = new Extension();

		for (Entry<String, String> entry : map.entrySet()) {
			e.setDynamicAttribute(entry.getKey(), entry.getValue());
		}
		property.addExtension(e);
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
				Map<String, String> diskRootMap = new HashMap<String, String>();

				diskRootMap.put("Name", root.getAbsolutePath());
				appendExtension(diskVolume, diskRootMap);
			}
		}

		File data = new File(m_dataPath);

		if (data.exists()) {
			Map<String, String> diskDataMap = new HashMap<String, String>();

			diskDataMap.put("Name", data.getAbsolutePath());
			appendExtension(diskVolume, diskDataMap);
		}
	}

	public StatusInfoCollector setDumpLocked(boolean dumpLocked) {
		m_dumpLocked = dumpLocked;
		return this;
	}

	private void setMemoryInfo(StatusInfo status) {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		Runtime runtime = Runtime.getRuntime();

		status.findOrCreateProperty("Max").setValue(Long.toString(runtime.maxMemory()));
		status.findOrCreateProperty("Total").setValue(Long.toString(runtime.totalMemory()));
		status.findOrCreateProperty("Free").setValue(Long.toString(runtime.freeMemory()));
		status.findOrCreateProperty("HeapUsage").setValue(Long.toString(memoryBean.getHeapMemoryUsage().getUsed()));
		status.findOrCreateProperty("NonHeapUsage").setValue(Long.toString(memoryBean.getNonHeapMemoryUsage().getUsed()));

		for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
			if (mpBean.getName().contains("Eden")) {
				status.findOrCreateProperty("EdenUsage").setValue(Long.toString(mpBean.getUsage().getUsed()));
			} else if (mpBean.getName().contains("Survivor")) {
				status.findOrCreateProperty("SurvivorUsage").setValue(Long.toString(mpBean.getUsage().getUsed()));
			}
		}

		List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
		Property gc = status.findOrCreateProperty("GC");

		for (GarbageCollectorMXBean mxbean : beans) {
			if (mxbean.isValid()) {
				Map<String, String> gcMap = new HashMap<String, String>();

				gcMap.put("Name", mxbean.getName());
				gcMap.put("Count", Long.toString(mxbean.getCollectionCount()));
				gcMap.put("Time", Long.toString(mxbean.getCollectionTime()));

				appendExtension(gc, gcMap);
			}
		}
	}

	private void setMessageInfo(StatusInfo status) {
		if (m_statistics != null) {
			status.findOrCreateProperty("Produced").setValue(Long.toString(m_statistics.getProduced()));
			status.findOrCreateProperty("Overflowed").setValue(Long.toString(m_statistics.getOverflowed()));
			status.findOrCreateProperty("Bytes").setValue(Long.toString(m_statistics.getBytes()));
		}
	}

	private void setOsInfo(StatusInfo status) {
		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

		status.findOrCreateProperty("Arch").setValue(bean.getArch());
		status.findOrCreateProperty("Name").setValue(bean.getName());
		status.findOrCreateProperty("Version").setValue(bean.getVersion());
		status.findOrCreateProperty("AvailableProcessors").setValue(Integer.toString(bean.getAvailableProcessors()));
		status.findOrCreateProperty("SystemLoadAverage").setValue(Double.toString(bean.getSystemLoadAverage()));

		// for Sun JDK
		if (isInstanceOfInterface(bean.getClass(), "com.sun.management.OperatingSystemMXBean")) {
			com.sun.management.OperatingSystemMXBean b = (com.sun.management.OperatingSystemMXBean) bean;

			status.findOrCreateProperty("TotalPhysicalMemory").setValue(Long.toString(b.getTotalPhysicalMemorySize()));
			status.findOrCreateProperty("FreePhysicalMemory").setValue(Long.toString(b.getFreePhysicalMemorySize()));
			status.findOrCreateProperty("TotalSwapSpace").setValue(Long.toString(b.getTotalSwapSpaceSize()));
			status.findOrCreateProperty("FreeSwapSpace").setValue(Long.toString(b.getFreeSwapSpaceSize()));
			status.findOrCreateProperty("ProcessTime").setValue(Long.toString(b.getProcessCpuTime()));
			status.findOrCreateProperty("CommittedVirtualMemory").setValue(
			      Long.toString(b.getCommittedVirtualMemorySize()));
		}
	}

	private void setRuntimeInfo(StatusInfo status) {
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

		status.findOrCreateProperty("StartTime").setValue(Long.toString(runtimeBean.getStartTime()));
		status.findOrCreateProperty("UpTime").setValue(Long.toString(runtimeBean.getUptime()));
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

		status.findOrCreateProperty("Count").setValue(Integer.toString(threadbean.getThreadCount()));
		status.findOrCreateProperty("DaemonCount").setValue(Integer.toString(threadbean.getDaemonThreadCount()));
		status.findOrCreateProperty("PeekCount").setValue(Integer.toString(threadbean.getPeakThreadCount()));
		status.findOrCreateProperty("TotalStartedCount").setValue(Long.toString(threadbean.getTotalStartedThreadCount()));
		status.findOrCreateProperty("CatThreadCount").setValue(Integer.toString(countThreadsByPrefix(threads, "Cat-")));
		status.findOrCreateProperty("PigeonThreadCount").setValue(
		      Integer.toString(countThreadsByPrefix(threads, "Pigeon-", "DPSF-", "Netty-", "Client-ResponseProcessor")));

		int jbossThreadsCount = countThreadsByPrefix(threads, "http-", "catalina-exec-");
		int jettyThreadsCount = countThreadsBySubstring(threads, "@qtp");

		status.findOrCreateProperty("HttpThreadCount").setValue(Integer.toString(jbossThreadsCount + jettyThreadsCount));
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