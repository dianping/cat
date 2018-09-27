package com.dianping.cat.report.page.app.display;

import java.util.Comparator;

import com.dianping.cat.report.page.app.QueryType;

public class AppDetailComparator implements Comparator<AppDataDetail> {

	private QueryType m_sortBy;

	public AppDetailComparator() {
		this(QueryType.REQUEST.getName());
	}

	public AppDetailComparator(String sortBy) {
		m_sortBy = QueryType.findByName(sortBy);
	}

	@Override
	public int compare(AppDataDetail o1, AppDataDetail o2) {
		switch (m_sortBy) {
		case DELAY:
			return (int) ((o2.getResponseTimeAvg() - o1.getResponseTimeAvg()) * 1000);
		case REQUEST:
			return (int) (o2.getAccessNumberSum() - o1.getAccessNumberSum());
		case REQUEST_PACKAGE:
			return (int) ((o2.getRequestPackageAvg() - o1.getRequestPackageAvg()) * 1000);
		case RESPONSE_PACKAGE:
			return (int) ((o2.getResponsePackageAvg() - o1.getResponsePackageAvg()) * 1000);
		case NETWORK_SUCCESS:
			return (int) ((o2.getSuccessRatio() - o1.getSuccessRatio()) * 1000);
		case BUSINESS_SUCCESS:
			return (int) ((o2.getBusinessSuccessRatio() - o1.getBusinessSuccessRatio()) * 1000);
		}
		return 0;
	}
}