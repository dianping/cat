package com.dianping.cat.report.page.appstats;

import java.util.Map;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.configuration.mobile.entity.ConstantItem;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.appstats.display.DisplayCommands;

@ModelMeta("AppStats")
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private AppReport m_appReport;

	@EntityMeta
	private Map<String, PieChart> m_piecharts;

	private Set<String> m_codeDistributions;

	private DisplayCommands m_displayCommands;

	private ConstantItem m_constantsItem;

	public Model(Context ctx) {
		super(ctx);
	}

	public AppReport getAppReport() {
		return m_appReport;
	}

	public Set<String> getCodeDistributions() {
		return m_codeDistributions;
	}

	public ConstantItem getConstantsItem() {
		return m_constantsItem;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public DisplayCommands getDisplayCommands() {
		return m_displayCommands;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	public Map<String, PieChart> getPiecharts() {
		return m_piecharts;
	}

	public void setAppReport(AppReport appReport) {
		m_appReport = appReport;
	}

	public void setCodeDistributions(Set<String> codeDistributions) {
		m_codeDistributions = codeDistributions;
	}

	public void setConstantsItem(ConstantItem constantsItem) {
		m_constantsItem = constantsItem;
	}

	public void setDisplayCommands(DisplayCommands displayCommands) {
		m_displayCommands = displayCommands;
	}

	public void setPiecharts(Map<String, PieChart> piecharts) {
		m_piecharts = piecharts;
	}
}
