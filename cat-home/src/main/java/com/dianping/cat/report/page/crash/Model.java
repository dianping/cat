package com.dianping.cat.report.page.crash;

import java.util.Collection;
import java.util.List;

import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.mobile.entity.Item;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.crash.display.CrashLogDetailInfo;
import com.dianping.cat.report.page.crash.display.CrashLogDisplayInfo;
import com.dianping.cat.report.page.crash.display.DisplayVersion;

@ModelMeta(Constants.CRASH)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private CrashLogDetailInfo m_crashLogDetailInfo;

	private CrashLogDisplayInfo m_crashLogDisplayInfo;

	private String m_fetchData;

	private Collection<Item> m_appNames;

	private List<DisplayVersion> m_versions;

	public Model(Context ctx) {
		super(ctx);
	}

	public List<DisplayVersion> getVersions() {
		return m_versions;
	}

	public void setVersions(List<DisplayVersion> versions) {
		m_versions = versions;
	}

	public Collection<Item> getAppNames() {
		return m_appNames;
	}

	public CrashLogDetailInfo getCrashLogDetailInfo() {
		return m_crashLogDetailInfo;
	}

	public CrashLogDisplayInfo getCrashLogDisplayInfo() {
		return m_crashLogDisplayInfo;
	}

	@Override
	public Action getDefaultAction() {
		return Action.APP_CRASH_LOG;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	public String getFetchData() {
		return m_fetchData;
	}

	public void setAppNames(Collection<Item> appNames) {
		m_appNames = appNames;
	}

	public void setCrashLogDetailInfo(CrashLogDetailInfo crashLogDetailInfo) {
		m_crashLogDetailInfo = crashLogDetailInfo;
	}

	public void setCrashLogDisplayInfo(CrashLogDisplayInfo crashLogDisplayInfo) {
		m_crashLogDisplayInfo = crashLogDisplayInfo;
	}

	public void setFetchData(String fetchData) {
		m_fetchData = fetchData;
	}
}
