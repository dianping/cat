package com.dianping.cat.report.page.service.provider;

import java.util.List;
import java.util.Map;

public interface ModelProvider {
	public String getModel(Map<String,String> parameters);
	
	public List<String> getDomains();
	
	public String getDefaultDomain();
	
}
