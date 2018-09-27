package org.unidal.cat.message.storage.hdfs;

import java.util.Set;

public interface MessageConsumerFinder {

	public Set<String> findConsumerIps(String domain, int hour);

}
