package com.dianping.cat.report.page.cdn.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CdnConfig {
	private static final Map<String, String> CDNS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("10.1.1.1", "帝联");
			put("10.1.1.2", "网宿");
		}
	};
	
	public final String GROUP = "cdn";
	
	public String getCdnName(String vip) {
		return CDNS.get(vip);
	}
	
	public List<String> getAllCdnNames() {
		List<String> cdns = new ArrayList<String>();
		
		for (Entry<String, String> entry : CDNS.entrySet()) {
			cdns.add(entry.getValue());
		}
		return cdns;
	}
	
	public boolean validateVip(String vip) {
		return CDNS.containsKey(vip);
	}
}
