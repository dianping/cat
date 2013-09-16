package com.dianping.cat.abtest.spi.internal;

import java.util.Map;
import java.util.Set;

public interface ABTestCodec {
	public String encode(Map<String, Map<String, String>> map);

	public String encode(String runId, Map<String, String> map);
	
	public Map<String, String> decode(String value);
	
	public Map<String, Map<String, String>> decode(String value, Set<String> keys);

}
