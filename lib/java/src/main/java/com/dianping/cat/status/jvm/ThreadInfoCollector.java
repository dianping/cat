package com.dianping.cat.status.jvm;

import com.dianping.cat.status.AbstractCollector;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

public class ThreadInfoCollector extends AbstractCollector {

	private int countThreadsByPrefix(ThreadInfo[] threads, String... prefixes) {
		int count = 0;

		for (ThreadInfo thread : threads) {
			if (thread != null) {
				for (String prefix : prefixes) {
					if (String.valueOf(thread.getThreadName()).startsWith(prefix)) {
						count++;
					}
				}
			}
		}

		return count;
	}

	private int countThreadsBySubstring(ThreadInfo[] threads, String... substrings) {
		int count = 0;

		for (ThreadInfo thread : threads) {
			if (thread != null) {
				for (String str : substrings) {
					if (String.valueOf(thread.getThreadName()).contains(str)) {
						count++;
					}
				}
			}
		}

		return count;
	}

	private Map<String, Number> doThreadCollect() {
		final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		Map<String, Number> map = new LinkedHashMap<String, Number>();
		map.put("jvm.thread.count", threadBean.getThreadCount());
		map.put("jvm.thread.daemon.count", threadBean.getDaemonThreadCount());
		map.put("jvm.thread.totalstarted.count", threadBean.getTotalStartedThreadCount());
		ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadBean.getAllThreadIds());

		int newThreadCount = 0;
		int runnableThreadCount = 0;
		int blockedThreadCount = 0;
		int waitThreadCount = 0;
		int timeWaitThreadCount = 0;
		int terminatedThreadCount = 0;

		if (threadInfos != null) {
			for (ThreadInfo threadInfo : threadInfos) {
				if (threadInfo != null) {
					switch (threadInfo.getThreadState()) {
					case NEW:
						newThreadCount++;
						break;
					case RUNNABLE:
						runnableThreadCount++;
						break;
					case BLOCKED:
						blockedThreadCount++;
						break;
					case WAITING:
						waitThreadCount++;
						break;
					case TIMED_WAITING:
						timeWaitThreadCount++;
						break;
					case TERMINATED:
						terminatedThreadCount++;
						break;
					default:
						break;
					}
				} else {
					/**
					 * If a thread of a given ID is not alive or does not exist, the corresponding element in the returned array will,
					 * contain null,because is mut exist ,so the thread is terminated
					 */
					terminatedThreadCount++;
				}
			}
		}

		map.put("jvm.thread.new.count", newThreadCount);
		map.put("jvm.thread.runnable.count", runnableThreadCount);
		map.put("jvm.thread.blocked.count", blockedThreadCount);
		map.put("jvm.thread.waiting.count", waitThreadCount);
		map.put("jvm.thread.time_waiting.count", timeWaitThreadCount);
		map.put("jvm.thread.terminated.count", terminatedThreadCount);

		long[] ids = threadBean.findDeadlockedThreads();

		map.put("jvm.thread.deadlock.count", ids == null ? 0 : ids.length);

		if (threadInfos != null) {
			int tomcatThreadsCount = countThreadsByPrefix(threadInfos, "http-", "catalina-exec-");
			int jettyThreadsCount = countThreadsBySubstring(threadInfos, "@qtp");

			map.put("jvm.thread.http.count", tomcatThreadsCount + jettyThreadsCount);
			map.put("jvm.thread.cat.count", countThreadsByPrefix(threadInfos, "Cat-", "cat-"));
			map.put("jvm.thread.pigeon.count",
			      countThreadsByPrefix(threadInfos, "Pigeon-", "DPSF-", "Client-ResponseProcessor"));
		}

		return map;
	}

	@Override
	public String getId() {
		return "jvm.thread";
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, Number> map = doThreadCollect();

		return convert(map);
	}
}
