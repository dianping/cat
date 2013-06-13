package com.dianping.cat.abtest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.PlexusContainer;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.abtest.internal.DefaultABTest;
import com.dianping.cat.abtest.repository.ABTestEntityRepository;
import com.dianping.cat.abtest.spi.internal.ABTestContextManager;

public final class ABTestManager {
	private static ABTestContextManager s_contextManager;

	public static ABTest getTest(ABTestName name) {
		initialize();

		return new DefaultABTest(name, s_contextManager);
	}

	public static void initialize() {
		if (s_contextManager == null) {
			synchronized (ABTestManager.class) {
				if (s_contextManager == null) {
					try {
						// it could be time-consuming due to load entities from the repository, i.e. database.
						PlexusContainer container = ContainerLoader.getDefaultContainer();

						s_contextManager = container.lookup(ABTestContextManager.class);

						ABTestEntityRepository repository = container.lookup(ABTestEntityRepository.class);

						if (repository instanceof Task) {
							Threads.forGroup("Cat").start((Task) repository);
						}
					} catch (Exception e) {
						throw new RuntimeException("Error when initializing ABTestContextManager!", e);
					}
				}
			}
		}
	}

	public static void onRequestBegin(HttpServletRequest request,HttpServletResponse response) {
		initialize();

		s_contextManager.onRequestBegin(request,response);
	}

	public static void onRequestEnd() {
		s_contextManager.onRequestEnd();
	}
}
