package com.dianping.cat.report.view;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.Page;

public class NavigationBar {
	public Page[] getVisiblePages() {
		return new Page[] {

		ReportPage.HOME,

<<<<<<< HEAD
=======
		//ReportPage.FAILURE,

>>>>>>> 1ce8aecf691c885c4773c6c52388dad926ad9557
		ReportPage.TRANSACTION,
		
		ReportPage.PROBLEM,
		
		ReportPage.IP,
<<<<<<< HEAD
=======

		ReportPage.SQL,
		//ReportPage.SERVICE,
>>>>>>> 1ce8aecf691c885c4773c6c52388dad926ad9557
		
		ReportPage.LOGVIEW

		};
	}
}
