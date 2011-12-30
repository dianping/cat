package com.dianping.cat.message.consumer.impl;

import org.apache.log4j.Logger;

import com.dianping.cat.message.consumer.failure.FailureReportMessageAnalyzer;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

/**
 * The consumer is used to record the error and exception state of the system。
 * 
 * The consumer records the lasteast info of one hour。
 * 
 * 考虑问题： 1、Consumer能支持domain进行拆分部署多台物理机。 2、不需要考虑内存数据的稳定性，报表按照每个小时出一份。
 * 3、需要考虑Consumer不能影响到其他的程序，添加一个Message需要立即返回。
 * 4、需要考虑后续处理Consumer的线程稳定性，尽量使用一个线程处理一段时间内的东西并且返回。
 * duration、Queue可以尽量分为3个类型，上一个队列，正在处理的队列，下一个队列，他们可以循环利用。
 * 6、内存中一个小时的数据存放问题，提供Service给外界访问。 7、启动线程更新报表的时钟KEY信息。
 * 
 * @author yong.you
 * 
 */
public class RealtimeTask {	

	private static Logger logger = Logger.getLogger(RealtimeTask.class);
	private RealtimeConsumerConfig m_config;
	private DefaultMessageQueue m_firstQueue;
	private DefaultMessageQueue m_secondQueue;
	
	//传进来业务自己实现的MessageAnalyzer的Class类。
	@Inject 
	private String className; 
	
	public RealtimeTask(RealtimeConsumerConfig config) {
		long currentTimeMillis = System.currentTimeMillis();
		long lastHour = currentTimeMillis - currentTimeMillis% m_config.getQueueTime();
		m_firstQueue = new DefaultMessageQueue(m_config.getDuration(), lastHour);
		m_secondQueue = new DefaultMessageQueue(m_config.getDuration(), lastHour+m_config.getQueueTime());
		startThread(m_firstQueue);
		startThread(m_secondQueue);
	}

	/**
	 * 将MessageTree放入Queue中。 将下一个小时MessageTree放入下一个Queue，new一个Queue，new一个Thread。
	 */
	public void consume(MessageTree tree) {
		if(m_firstQueue.isExpired()){
			switchQueue();
			consume(tree);
		}
		else{
			if(m_firstQueue.inRange(tree)){
				m_firstQueue.offer(tree);
			}
			else if(m_secondQueue.inRange(tree)){
				m_secondQueue.offer(tree);
			}
			else {
				logger.error("Discard it "+ tree);		
			}
		}
	}

	public void switchQueue(){
		long secondQueueTime = m_secondQueue.getStart();
		m_firstQueue  = m_secondQueue;
		m_secondQueue = new DefaultMessageQueue(m_config.getDuration(), secondQueueTime+m_config.getQueueTime());	
		startThread(m_secondQueue);
	}
	
	private void startThread(final MessageQueue queue){
		// 根据传进来的ClassName生成一个实例
		final FailureReportMessageAnalyzer analyzer = new FailureReportMessageAnalyzer();
		Thread thread=new Thread(new Runnable() {
			@Override
			public void run() {
				analyzer.analyze(queue);				
			}
		});
		thread.start();
	}
	
	
}
