package com.dianping.cat.message.spi;

public interface MessageFilter {
	public String getConsumerId();

	public boolean doFilter(byte[] data);
}
