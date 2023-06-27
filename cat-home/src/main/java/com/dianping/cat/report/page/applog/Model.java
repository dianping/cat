package com.dianping.cat.report.page.applog;

import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.applog.display.AppLogDetailInfo;
import com.dianping.cat.report.page.applog.display.AppLogDisplayInfo;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private AppLogDisplayInfo m_appLogDisplayInfo;

	private AppLogDetailInfo m_appLogDetailInfo;

	public Model(Context ctx) {
		super(ctx);
	}

	public AppLogDetailInfo getAppLogDetailInfo() {
		return m_appLogDetailInfo;
	}

	public void setAppLogDetailInfo(AppLogDetailInfo appLogDetailInfo) {
		m_appLogDetailInfo = appLogDetailInfo;
	}

	public AppLogDisplayInfo getAppLogDisplayInfo() {
		return m_appLogDisplayInfo;
	}

	public void setAppLogDisplayInfo(AppLogDisplayInfo appLogDisplayInfo) {
		m_appLogDisplayInfo = appLogDisplayInfo;
	}

	@Override
	public Action getDefaultAction() {
		return Action.APP_LOG;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}
}
