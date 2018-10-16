/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.TreeMap;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.model.entity.DiskInfo;
import com.dianping.cat.status.model.entity.DiskVolumeInfo;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.GcInfo;
import com.dianping.cat.status.model.entity.MemoryInfo;
import com.dianping.cat.status.model.entity.MessageInfo;
import com.dianping.cat.status.model.entity.OsInfo;
import com.dianping.cat.status.model.entity.RuntimeInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadsInfo;
import com.dianping.cat.status.model.transform.BaseVisitor;

public class StatusInfoCollector extends BaseVisitor {
	private MessageStatistics m_statistics;

	private boolean m_dumpLocked;

	private String m_jars;

	private String m_dataPath = "/data";

	private StatusInfo m_statusInfo;

	private String m_jstackInfo;

	public StatusInfoCollector(MessageStatistics statistics, String jars) {
		m_statistics = statistics;
		m_jars = jars;
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

	public String getJstackInfo() {
		return m_jstackInfo;
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

	public StatusInfoCollector setDumpLocked(boolean dumpLocked) {
		m_dumpLocked = dumpLocked;
		return this;
	}

	@Override
	public void visitDisk(DiskInfo disk) {
		File[] roots = File.listRoots();

		if (roots != null) {
			for (File root : roots) {
				disk.addDiskVolume(new DiskVolumeInfo(root.getAbsolutePath()));
			}
		}

		File data = new File(m_dataPath);

		if (data.exists()) {
			disk.addDiskVolume(new DiskVolumeInfo(data.getAbsolutePath()));
		}

		super.visitDisk(disk);
	}

	@Override
	public void visitDiskVolume(DiskVolumeInfo diskVolume) {
		Extension diskExtension = m_statusInfo.findOrCreateExtension("Disk");
		File volume = new File(diskVolume.getId());

		diskVolume.setTotal(volume.getTotalSpace());
		diskVolume.setFree(volume.getFreeSpace());
		diskVolume.setUsable(volume.getUsableSpace());

		diskExtension.findOrCreateExtensionDetail(diskVolume.getId() + " Free").setValue(volume.getFreeSpace());
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
		Extension gcExtension = m_statusInfo.findOrCreateExtension("GC");

		for (GarbageCollectorMXBean mxbean : beans) {
			if (mxbean.isValid()) {
				GcInfo gc = new GcInfo();
				String name = mxbean.getName();
				long count = mxbean.getCollectionCount();

				gc.setName(name);
				gc.setCount(count);
				gc.setTime(mxbean.getCollectionTime());
				memory.addGc(gc);

				gcExtension.findOrCreateExtensionDetail(name + "Count").setValue(count);
				gcExtension.findOrCreateExtensionDetail(name + "Time").setValue(mxbean.getCollectionTime());
			}
		}
		Extension heapUsage = m_statusInfo.findOrCreateExtension("JVMHeap");

		for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
			long count = mpBean.getUsage().getUsed();
			String name = mpBean.getName();

			heapUsage.findOrCreateExtensionDetail(name).setValue(count);
		}

		super.visitMemory(memory);
	}

	@Override
	public void visitMessage(MessageInfo message) {
		Extension catExtension = m_statusInfo.findOrCreateExtension("CatUsage");

		if (m_statistics != null) {
			catExtension.findOrCreateExtensionDetail("Produced").setValue(m_statistics.getProduced());
			catExtension.findOrCreateExtensionDetail("Overflowed").setValue(m_statistics.getOverflowed());
			catExtension.findOrCreateExtensionDetail("Bytes").setValue(m_statistics.getBytes());
		}
	}

	@Override
	public void visitOs(OsInfo os) {
		Extension systemExtension = m_statusInfo.findOrCreateExtension("System");
		OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

		os.setArch(bean.getArch());
		os.setName(bean.getName());
		os.setVersion(bean.getVersion());
		os.setAvailableProcessors(bean.getAvailableProcessors());
		os.setSystemLoadAverage(bean.getSystemLoadAverage());

		systemExtension.findOrCreateExtensionDetail("LoadAverage").setValue(bean.getSystemLoadAverage());

		// for Sun JDK
		if (isInstanceOfInterface(bean.getClass(), "com.sun.management.OperatingSystemMXBean")) {
			com.sun.management.OperatingSystemMXBean b = (com.sun.management.OperatingSystemMXBean) bean;

			os.setTotalPhysicalMemory(b.getTotalPhysicalMemorySize());
			os.setFreePhysicalMemory(b.getFreePhysicalMemorySize());
			os.setTotalSwapSpace(b.getTotalSwapSpaceSize());
			os.setFreeSwapSpace(b.getFreeSwapSpaceSize());
			os.setProcessTime(b.getProcessCpuTime());
			os.setCommittedVirtualMemory(b.getCommittedVirtualMemorySize());

			systemExtension.findOrCreateExtensionDetail("FreePhysicalMemory").setValue(b.getFreePhysicalMemorySize());
			systemExtension.findOrCreateExtensionDetail("FreeSwapSpaceSize").setValue(b.getFreeSwapSpaceSize());
		}
		m_statusInfo.addExtension(systemExtension);
	}

	@Override
	public void visitRuntime(RuntimeInfo runtime) {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();

		runtime.setStartTime(bean.getStartTime());
		runtime.setUpTime(bean.getUptime());
		runtime.setJavaClasspath(m_jars);
		runtime.setJavaVersion(System.getProperty("java.version"));
		runtime.setUserDir(System.getProperty("user.dir"));
		runtime.setUserName(System.getProperty("user.name"));
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
		m_statusInfo = status;

		super.visitStatus(status);
	}

	@Override
	public void visitThread(ThreadsInfo thread) {
		Extension frameworkThread = m_statusInfo.findOrCreateExtension("FrameworkThread");
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();

		bean.setThreadContentionMonitoringEnabled(true);

		ThreadInfo[] threads;

		if (m_dumpLocked) {
			threads = bean.dumpAllThreads(true, true);
		} else {
			threads = bean.dumpAllThreads(false, false);
		}

		thread.setCount(bean.getThreadCount());
		thread.setDaemonCount(bean.getDaemonThreadCount());
		thread.setPeekCount(bean.getPeakThreadCount());
		thread.setTotalStartedCount((int) bean.getTotalStartedThreadCount());

		int jbossThreadsCount = countThreadsByPrefix(threads, "http-", "catalina-exec-");
		int jettyThreadsCount = countThreadsBySubstring(threads, "@qtp");

		m_jstackInfo = getThreadDump(threads);

		frameworkThread.findOrCreateExtensionDetail("HttpThread").setValue(jbossThreadsCount + jettyThreadsCount);
		frameworkThread.findOrCreateExtensionDetail("CatThread").setValue(countThreadsByPrefix(threads, "Cat-"));
		frameworkThread.findOrCreateExtensionDetail("PigeonThread")
								.setValue(countThreadsByPrefix(threads, "Pigeon-", "DPSF-", "Netty-", "Client-ResponseProcessor"));
		frameworkThread.findOrCreateExtensionDetail("ActiveThread").setValue(bean.getThreadCount());
		frameworkThread.findOrCreateExtensionDetail("StartedThread").setValue(bean.getTotalStartedThreadCount());

		m_statusInfo.addExtension(frameworkThread);
	}

}