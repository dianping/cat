package com.dianping.cat.report.page.cdn.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CdnConfig {

	public static final String GROUP = "cdn";

	private static final Map<String, String> CDNS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("180.153.132.55", "帝联");
			put("180.153.132.56", "网宿");
		}
	};

	public List<String> queryAllCdnNames() {
		List<String> cdns = new ArrayList<String>();

		for (Entry<String, String> entry : CDNS.entrySet()) {
			cdns.add(entry.getValue());
		}
		return cdns;
	}

	public String queryCdnName(String vip) {
		return CDNS.get(vip);
	}

	public boolean validateVip(String vip) {
		return CDNS.containsKey(vip);
	}
}
