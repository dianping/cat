package com.dianping.cat.report.view;

import org.unidal.web.mvc.Page;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.system.SystemPage;

public class NavigationBar {
	public Page[] getSystemPages() {
		return new Page[] {

		SystemPage.CONFIG,

		SystemPage.LOGIN

		};
	}

	public Page[] getVisiblePages() {
		return new Page[] {

		ReportPage.WEB,

		ReportPage.APP,

		ReportPage.METRIC,

		ReportPage.TRANSACTION,

		ReportPage.EVENT,

		ReportPage.PROBLEM,

		ReportPage.HEARTBEAT,

		ReportPage.CROSS,

		ReportPage.CACHE,

		ReportPage.DEPENDENCY,

		ReportPage.STATE,

		ReportPage.LOGVIEW

		};
	}
}
