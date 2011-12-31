package com.dianping.cat.message.consumer.impl;

public class RealtimeConsumerConfig {
	private static RealtimeConsumerConfig config;
	private String m_domain;
	private int m_queueTime;
	private int m_duration;
	private String m_consumerName;
	private String anaylyzerClassName;
	
	private static final String DEFAULT_CONFIG = "realtimeConsumer.property";
	private static boolean IS_DEFAULT = true;
	
	public static synchronized RealtimeConsumerConfig getConfig() {
		if (config == null) {
			
		}
		return config;
	}

	public static synchronized RealtimeConsumerConfig getConfig(String fileName) {
		if (config == null) {

		}
		return config;
	}
	public boolean containsDomain(String domain){
		if(IS_DEFAULT)
			return true;
		return true;
	}

	public String getDomain() {
		return m_domain;
	}

	public int getQueueTime() {
		return m_queueTime;
	}

	public int getDuration() {
		return m_duration;
	}

	public String getConsumerName() {
		return m_consumerName;
	}
	
}
