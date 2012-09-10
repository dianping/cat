package com.dianping.dog.alarm.merge;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RuleExecutorThreadPool {
	
	private ThreadPoolExecutor executor ;
	private DefaultThreadFactory factory;
	
	protected RuleExecutorThreadPool(String poolName){
		this.executor = (ThreadPoolExecutor)Executors.newCachedThreadPool(new DefaultThreadFactory(poolName));
	}
	
	protected RuleExecutorThreadPool(String poolName,int corePoolSize,int maximumPoolSize){
		this.factory = new DefaultThreadFactory(poolName);
		this.executor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,
												 60L, TimeUnit.SECONDS,
								                 new SynchronousQueue<Runnable>(),
								                 this.factory
								                 );
	}
	
	protected RuleExecutorThreadPool(String poolName,int corePoolSize,int maximumPoolSize,
			BlockingQueue<Runnable> workQueue){
		this.factory = new DefaultThreadFactory(poolName);
		this.executor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,
												 60L, TimeUnit.SECONDS,
												 workQueue,
								                 this.factory
								                 );
	}
	
	public RuleExecutorThreadPool(String poolName, int corePoolSize, int maximumPoolSize, BlockingQueue<Runnable> workQueue,
			RejectedExecutionHandler handler) {
		this.factory = new DefaultThreadFactory(poolName);
		this.executor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,
												 60L, TimeUnit.SECONDS,
												 workQueue,
								                 this.factory,
								                 handler);
	}

	public void execute(Runnable run){
		this.executor.execute(run);
	}
	
	public <T> Future<T> submit(Callable<T> call){
		return this.executor.submit(call);
	}
	
	@SuppressWarnings("rawtypes")
   public Future submit(Runnable run){
		return this.executor.submit(run);
	}
	
	public ThreadPoolExecutor getExecutor(){
		return this.executor;
	}

	/**
	 * @return the factory
	 */
	public DefaultThreadFactory getFactory() {
		return factory;
	}

}
