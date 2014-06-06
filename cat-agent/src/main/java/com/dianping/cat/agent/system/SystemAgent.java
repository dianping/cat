package com.dianping.cat.agent.system;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemAgent {

	public static final int THREAD_NUM = 3;

	public static void main(String args[]) {

		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUM);

		String filePath = System.getProperty("config.file");
		System.out.println(filePath);
		Configuration config = new Configuration();
		config.load(filePath);
		
		executorService.execute(new PerformanceTask(config));
		executorService.execute(new StateTask(config));
		executorService.execute(new JVMTask(config));
		executorService.shutdown();
	}
}
