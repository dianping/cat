package com.dianping.tkv;

import java.util.Collection;


public interface MetaHolder {

	Collection<String> getKeys();

	void getMeta(String key, Meta meta);

}
