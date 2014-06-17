package com.dianping.cat.agent.monitor.executors;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.agent.monitor.EnvironmentConfig;

public abstract class AbstractExecutor implements Executor {

	@Inject
	protected EnvironmentConfig m_environmentConfig;

	public static final String SUM_TYPE = "sum";

	public static final String AVG_TYPE = "avg";

	public static final String COUNT_TYPE = "count";

	public static final String JVM_TYPE = "jvm";

	public static final String SYSTEM_TYPE = "system";

	public static final String NGINX_TYPE = "nginx";

	protected String buildSystemDataEntityId(String id) {
		return SYSTEM_TYPE + "_" + id + "_" + m_environmentConfig.getIp();
	}

	protected String buildJVMDataEntityId(String id) {
		return JVM_TYPE + "_" + id + "_" + m_environmentConfig.getIp();
	}

	protected String buildNginxDataEntityId(String id) {
		return NGINX_TYPE + "_" + id + "_" + m_environmentConfig.getIp();
	}

}
