package com.dianping.cat.message;

import java.io.Closeable;

public interface ForkedTransaction extends Transaction, Closeable {
	public static String FORKED = "Forked";

	public static String DETACHED = "Detached";

	public static String EMBEDDED = "Embedded";

	public void close();

	public String getMessageId();

	public String getParentMessageId();

	public String getRootMessageId();

	public ForkedTransaction join();

	public void setMessageId(String messageId);
}