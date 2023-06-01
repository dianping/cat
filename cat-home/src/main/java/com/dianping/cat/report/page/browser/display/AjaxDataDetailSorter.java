package com.dianping.cat.report.page.browser.display;

import com.dianping.cat.report.page.browser.service.AjaxQueryType;

import java.util.Comparator;

public class AjaxDataDetailSorter implements Comparator<AjaxDataDetail> {

	private AjaxQueryType m_sortBy;

	public AjaxDataDetailSorter(AjaxQueryType sortBy) {
		m_sortBy = sortBy;
	}

	@Override
	public int compare(AjaxDataDetail o1, AjaxDataDetail o2) {
		switch (m_sortBy) {
		case SUCCESS:
			return (int) ((o2.getSuccessRatio() - o1.getSuccessRatio()) * 1000);
		case REQUEST:
			return (int) (o2.getAccessNumberSum() - o1.getAccessNumberSum());
		case DELAY:
			return (int) ((o2.getResponseTimeAvg() - o1.getResponseTimeAvg()) * 1000);
		}
		return 0;
	}
}
