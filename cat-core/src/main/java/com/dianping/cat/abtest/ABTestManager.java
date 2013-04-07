package com.dianping.cat.abtest;

import javax.servlet.http.HttpServletRequest;

import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.abtest.internal.DefaultABTest;
import com.dianping.cat.abtest.spi.ABTestContextManager;

public final class ABTestManager {
	private static ABTestContextManager s_contextManager;

	public static ABTest getTest(ABTestId id) {
		initialize();

		return new DefaultABTest(id, s_contextManager);
	}

	public static void initialize() {
		if (s_contextManager == null) {
			synchronized (ABTestManager.class) {
				if (s_contextManager == null) {
					try {
						// it could be time-consuming due to load entities from the repository, i.e. database.
						s_contextManager = ContainerLoader.getDefaultContainer().lookup(ABTestContextManager.class);
					} catch (Exception e) {
						throw new RuntimeException("Error when initializing ABTestContextManager!", e);
					}
				}
			}
		}
	}

	public static void onRequestBegin(HttpServletRequest req) {
		initialize();

		s_contextManager.onRequestBegin(req);
	}

	public static void onRequestEnd() {
		s_contextManager.onRequestEnd();
	}
}
