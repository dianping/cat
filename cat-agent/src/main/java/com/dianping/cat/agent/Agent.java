package com.dianping.cat.agent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dianping.cat.agent.jvm.jvmTask;
import com.dianping.cat.agent.system.PerformanceTask;

public class Agent {

	public static final int THREAD_NUM = 3;

	public static void main(String args[]) {

		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUM);

		String filePath = System.getProperty("config.file");
		Configuration config = new Configuration();
		config.load(filePath);
		
		executorService.execute(new PerformanceTask(config));
		executorService.execute(new StateTask(config));
		executorService.execute(new jvmTask(config));
		executorService.shutdown();
	}
}
