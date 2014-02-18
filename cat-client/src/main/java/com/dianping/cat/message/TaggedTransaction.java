package com.dianping.cat.message;

public interface TaggedTransaction extends Transaction {
	public void bind(String tag, String childMessageId, String title);

	public String getParentMessageId();

	public String getRootMessageId();

	public String getTag();

	public void start();
}
