package com.dianping.cat.message.spi;

public interface MessageStatistics {
	public long getBytes();

	public long getOverflowed();

	public long getProduced();

	public void onBytes(int size);

	public void onOverflowed(MessageTree tree);

}
