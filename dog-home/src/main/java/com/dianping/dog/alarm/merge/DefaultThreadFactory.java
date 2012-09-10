package com.dianping.dog.alarm.merge;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory{

	static final AtomicInteger poolNumber = new AtomicInteger(1);
	final AtomicInteger threadNumber;
	final ThreadGroup group;
	final String namePrefix;
	final boolean isDaemon;

	public DefaultThreadFactory(){
	    this("Default-Pool"); 
	}

	public DefaultThreadFactory(String name) {
	    this(name, true);
	}

	public DefaultThreadFactory(String preffix, boolean daemon){
		this.threadNumber = new AtomicInteger(1);

	    this.group = new ThreadGroup(preffix + "-" + poolNumber.getAndIncrement() + "-threadGroup");

	    this.namePrefix = preffix + "-" + poolNumber.getAndIncrement() + "-thread-";
	    this.isDaemon = daemon;
	}

	public Thread newThread(Runnable r){
	    Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), -3715992351445876736L);

	    t.setDaemon(this.isDaemon);
	    if (t.getPriority() != 5)
	    	t.setPriority(5);

	    return t;
	}

	public ThreadGroup getGroup() {
		return group;
	}

}
