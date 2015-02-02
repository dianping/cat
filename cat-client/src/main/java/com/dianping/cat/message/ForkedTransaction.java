package com.dianping.cat.message;

public interface ForkedTransaction extends Transaction {
	public void fork();

	public String getForkedMessageId();
}
