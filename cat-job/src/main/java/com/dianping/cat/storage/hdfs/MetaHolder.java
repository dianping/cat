package com.dianping.cat.storage.hdfs;

import java.util.Collection;


public interface MetaHolder {

	Collection<String> getKeys();

	void getMeta(String key, Meta meta);

}
