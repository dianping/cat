package com.dianping.cat.report.view;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.Page;

public class NavigationBar {
	public Page[] getVisiblePages() {
		return new Page[] {

		ReportPage.HOME,

		ReportPage.TOP,

		ReportPage.TRANSACTION,

		ReportPage.EVENT,

		ReportPage.PROBLEM,

		ReportPage.HEARTBEAT,

		ReportPage.MATRIX,

		ReportPage.CROSS,

		ReportPage.CACHE,

		ReportPage.IP,

		ReportPage.HEATMAP,

		ReportPage.SQL,

		ReportPage.TASK,

		ReportPage.DATABASE,

		ReportPage.QUERY,

		ReportPage.HEALTH,

		ReportPage.STATE,

		ReportPage.LOGVIEW

		};
	}

	public Page[] getSystemPages() {
		return new Page[] {

		SystemPage.ALARM,

		SystemPage.PROJECT,

		SystemPage.LOGIN

		};
	}
}
