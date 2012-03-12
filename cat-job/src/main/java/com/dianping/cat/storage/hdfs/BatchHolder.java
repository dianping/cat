package com.dianping.cat.storage.hdfs;

import java.util.Collection;


public interface BatchHolder {

	Collection<String> getKeys();

	void getMeta(String key, Meta meta);
	
	byte[] getValue(String key);

}
