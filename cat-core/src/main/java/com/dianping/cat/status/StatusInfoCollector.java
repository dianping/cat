package com.dianping.cat.status;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.model.entity.DiskInfo;
import com.dianping.cat.status.model.entity.DiskVolumeInfo;
import com.dianping.cat.status.model.entity.GcInfo;
import com.dianping.cat.status.model.entity.MemoryInfo;
import com.dianping.cat.status.model.entity.MessageInfo;
import com.dianping.cat.status.model.entity.OsInfo;
import com.dianping.cat.status.model.entity.RuntimeInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadsInfo;
import com.dianping.cat.status.model.transform.BaseVisitor;

class StatusInfoCollector extends BaseVisitor {
	private MessageStatistics m_statistics;

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

	@Override
   public void visitDisk(DiskInfo disk) {
		File[] roots = File.listRoots();
		
		if (roots != null) {
			for (File root : roots) {
				disk.addDiskVolume(new DiskVolumeInfo(root.getAbsolutePath()));
			}
		}
		
	   super.visitDisk(disk);
   }

	@Override
	public void visitDiskVolume(DiskVolumeInfo diskVolume) {
		File volume = new File(diskVolume.getId());

		diskVolume.setTotal(volume.getTotalSpace());
		diskVolume.setFree(volume.getFreeSpace());
		diskVolume.setUsable(volume.getUsableSpace());
	}

	@Override
	public void visitMemory(MemoryInfo memory) {
		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
		Runtime runtime = Runtime.getRuntime();

		memory.setMax(runtime.maxMemory());
		memory.setTotal(runtime.totalMemory());
		memory.setFree(runtime.freeMemory());
		memory.setHeapUsage(bean.getHeapMemoryUsage().getUsed());
		memory.setNonHeapUsage(bean.getNonHeapMemoryUsage().getUsed());
		
		List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean mxbean : beans) {
			if (mxbean.isValid()) {
				GcInfo gc = new GcInfo();
				gc.setName(mxbean.getName());
				gc.setCount(mxbean.getCollectionCount());
				gc.setTime(mxbean.getCollectionTime());
				memory.addGc(gc);
			}
		}

		super.visitMemory(memory);
	}

	@Override
	public void visitMessage(MessageInfo message) {
		if (m_statistics != null) {
			message.setProduced(m_statistics.getProduced());
			message.setOverflowed(m_statistics.getOverflowed());
			message.setBytes(m_statistics.getBytes());
		}
	}

	@Override
	public void visitOs(OsInfo os) {
		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

		os.setArch(bean.getArch());
		os.setName(bean.getName());
		os.setVersion(bean.getName());
		os.setAvailableProcessors(bean.getAvailableProcessors());
		os.setSystemLoadAverage(bean.getSystemLoadAverage());

		// for Sun JDK
		if (isInstanceOfInterface(bean.getClass(), "com.sun.management.OperatingSystemMXBean")) {
			com.sun.management.OperatingSystemMXBean b = (com.sun.management.OperatingSystemMXBean) bean;

			os.setTotalPhysicalMemory(b.getTotalPhysicalMemorySize());
			os.setFreePhysicalMemory(b.getFreePhysicalMemorySize());
			os.setTotalSwapSpace(b.getTotalSwapSpaceSize());
			os.setFreeSwapSpace(b.getFreeSwapSpaceSize());
			os.setProcessTime(b.getProcessCpuTime());
			os.setCommittedVirtualMemory(b.getCommittedVirtualMemorySize());
		}
	}

	@Override
	public void visitRuntime(RuntimeInfo runtime) {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();

		runtime.setStartTime(bean.getStartTime());
		runtime.setUpTime(bean.getUptime());
	}

	@Override
	public void visitStatus(StatusInfo status) {
		status.setTimestamp(new Date());
		status.setOs(new OsInfo());
		status.setDisk(new DiskInfo());
		status.setRuntime(new RuntimeInfo());
		status.setMemory(new MemoryInfo());
		status.setThread(new ThreadsInfo());
		status.setMessage(new MessageInfo());

		super.visitStatus(status);
	}

	@Override
	public void visitThread(ThreadsInfo thread) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threads = bean.dumpAllThreads(true, true);

		thread.setCount(bean.getThreadCount());
		thread.setDaemonCount(bean.getDaemonThreadCount());
		thread.setPeekCount(bean.getPeakThreadCount());
		thread.setTotalStartedCount((int) bean.getTotalStartedThreadCount());
		thread.setCatThreadCount(countThreadsByPrefix(threads, "Cat-"));
		thread.setPigeonThreadCount(countThreadsByPrefix(threads, "Pigeon-", "DPSF-", "Netty-",
		      "Client-ResponseProcessor"));
		thread.setDump(getThreadDump(threads));
	}
}