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
import java.util.List;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.model.entity.Detail;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.transform.BaseVisitor;

public class StatusInfoCollector extends BaseVisitor {
	private MessageStatistics m_statistics;

	private boolean m_dumpLocked;

	private String m_dataPath = "/data";

	public StatusInfoCollector(MessageStatistics statistics) {
		m_statistics = statistics;
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

	private void setDiskFreeInfo(StatusInfo status) {
		Extension diskExtension = new Extension("DISK FREE");
		status.addExtension(diskExtension);

		File[] roots = File.listRoots();

		if (roots != null) {
			for (File root : roots) {
				diskExtension.addDetail(new Detail(root.getAbsolutePath()).setValue(root.getFreeSpace()));
			}
		}

		File data = new File(m_dataPath);

		if (data.exists()) {
			diskExtension.addDetail(new Detail(data.getAbsolutePath()).setValue(data.getFreeSpace()));
		}
	}

	public StatusInfoCollector setDumpLocked(boolean dumpLocked) {
		m_dumpLocked = dumpLocked;
		return this;
	}

	private void setMemoryInfo(StatusInfo status) {
		Extension memoryExtension = new Extension("MEMORY");
		status.addExtension(memoryExtension);
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		Runtime runtime = Runtime.getRuntime();

		memoryExtension.addDetail(new Detail("Max").setValue(runtime.maxMemory()));
		memoryExtension.addDetail(new Detail("Total").setValue(runtime.totalMemory()));
		memoryExtension.addDetail(new Detail("MemoryFree").setValue(runtime.freeMemory()));
		memoryExtension.addDetail(new Detail("HeapUsage").setValue(memoryBean.getHeapMemoryUsage().getUsed()));
		memoryExtension.addDetail(new Detail("NonHeapUsage").setValue(memoryBean.getNonHeapMemoryUsage().getUsed()));

		for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
			if (mpBean.getName().contains("Eden")) {
				memoryExtension.addDetail(new Detail("EdenUsage").setValue(mpBean.getUsage().getUsed()));
			} else if (mpBean.getName().contains("Survivor")) {
				memoryExtension.addDetail(new Detail("SurvivorUsage").setValue(mpBean.getUsage().getUsed()));
			}
		}

		List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();

		for (GarbageCollectorMXBean mxbean : beans) {
			if (mxbean.isValid()) {
				String beanName = mxbean.getName();
				long count = mxbean.getCollectionCount();

				if ("ParNew".equals(beanName) || "PS Scavenge".equals(beanName)) {
					memoryExtension.addDetail(new Detail("NewGcCount").setValue(count));
				} else if ("ConcurrentMarkSweep".equals(beanName) || "PS MarkSweep".equals(beanName)) {
					memoryExtension.addDetail(new Detail("OldGcCount").setValue(count));
				}
			}
		}
	}

	private void setMessageInfo(StatusInfo status) {
		if (m_statistics != null) {
			Extension messageExtension = new Extension("MESSAGE");
			status.addExtension(messageExtension);

			messageExtension.addDetail(new Detail("CatMessageProduced").setValue(m_statistics.getProduced()));
			messageExtension.addDetail(new Detail("CatMessageOverflowed").setValue(m_statistics.getOverflowed()));
			messageExtension.addDetail(new Detail("CatMessageSize").setValue(m_statistics.getBytes()));
		}
	}

	private void setOsInfo(StatusInfo status) {
		Extension osExtension = new Extension("OS");
		status.addExtension(osExtension);
		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

		osExtension.addDetail(new Detail("AvailableProcessors").setValue(bean.getAvailableProcessors()));
		osExtension.addDetail(new Detail("SystemLoadAverage").setValue(bean.getSystemLoadAverage()));

		// for Sun JDK
		if (isInstanceOfInterface(bean.getClass(), "com.sun.management.OperatingSystemMXBean")) {
			com.sun.management.OperatingSystemMXBean b = (com.sun.management.OperatingSystemMXBean) bean;

			osExtension.addDetail(new Detail("TotalPhysicalMemory").setValue(b.getTotalPhysicalMemorySize()));
			osExtension.addDetail(new Detail("FreePhysicalMemory").setValue(b.getFreePhysicalMemorySize()));
			osExtension.addDetail(new Detail("TotalSwapSpace").setValue(b.getTotalSwapSpaceSize()));
			osExtension.addDetail(new Detail("FreeSwapSpace").setValue(b.getFreeSwapSpaceSize()));
			osExtension.addDetail(new Detail("ProcessTime").setValue(b.getProcessCpuTime()));
			osExtension.addDetail(new Detail("CommittedVirtualMemory").setValue(b.getCommittedVirtualMemorySize()));
		}
	}

	private void setRuntimeInfo(StatusInfo status) {
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		Extension runtimeExtension = new Extension("RUNTIME");
		status.addExtension(runtimeExtension);

		runtimeExtension.addDetail(new Detail("StartTime").setValue(runtimeBean.getStartTime()));
		runtimeExtension.addDetail(new Detail("UpTime").setValue(runtimeBean.getUptime()));
	}

	private void setThreadInfo(StatusInfo status) {
		Extension threadExtension = new Extension("THREAD");
		status.addExtension(threadExtension);
		ThreadInfo[] threads;
		ThreadMXBean threadbean = ManagementFactory.getThreadMXBean();

		threadbean.setThreadContentionMonitoringEnabled(true);

		if (m_dumpLocked) {
			threads = threadbean.dumpAllThreads(true, true);
		} else {
			threads = threadbean.dumpAllThreads(false, false);
		}

		threadExtension.addDetail(new Detail("ThreadCount").setValue(threadbean.getThreadCount()));
		threadExtension.addDetail(new Detail("DaemonCount").setValue(threadbean.getDaemonThreadCount()));
		threadExtension.addDetail(new Detail("PeekCount").setValue(threadbean.getPeakThreadCount()));
		threadExtension.addDetail(new Detail("TotalStartedCount").setValue(threadbean.getTotalStartedThreadCount()));
		threadExtension.addDetail(new Detail("CatThreadCount").setValue(countThreadsByPrefix(threads, "Cat-")));
		threadExtension.addDetail(new Detail("PigeonThreadCount").setValue(countThreadsByPrefix(threads, "Pigeon-",
		      "DPSF-", "Netty-", "Client-ResponseProcessor")));

		int jbossThreadsCount = countThreadsByPrefix(threads, "http-", "catalina-exec-");
		int jettyThreadsCount = countThreadsBySubstring(threads, "@qtp");

		threadExtension.addDetail(new Detail("HttpThreadCount").setValue(jbossThreadsCount + jettyThreadsCount));
	}

	@Override
	public void visitStatus(StatusInfo status) {
		status.setTimestamp(new Date());

		setOsInfo(status);
		setDiskFreeInfo(status);
		setRuntimeInfo(status);
		setMemoryInfo(status);
		setThreadInfo(status);
		setMessageInfo(status);

		super.visitStatus(status);
	}

}