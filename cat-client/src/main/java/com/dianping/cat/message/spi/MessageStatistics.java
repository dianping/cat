package com.dianping.cat.message.spi;

public interface MessageStatistics {
	public long getProduced();

	public long getOverflowed();

	public long getBytes();

	public void onSending(MessageTree tree);

	public void onOverflowed(MessageTree tree);

	public void onBytes(int size);
}
