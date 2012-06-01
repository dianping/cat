package com.dianping.cat.report.page.trend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Model extends ViewModel<ReportPage, Action, Context> {

	private String m_domain;

	private List<String> m_domains;
	
	private String m_ip;

	private String m_graphType;

	private String m_queryIP;

	private String m_queryType;

	private String m_queryName;

	private String m_dateType;

	private String m_queryDate;

	private String m_selfQueryOption;

	private List<String> m_graphTypes;

	private String m_graph;
	
	public String getDateType() {
		return m_dateType;
	}

	public void setDateType(String dateType) {
		m_dateType = dateType;
	}

	public String getQueryDate() {
		return m_queryDate;
	}

	public void setQueryDate(String queryDate) {
		m_queryDate = queryDate;
	}

	public String getSelfQueryOption() {
		return m_selfQueryOption;
	}

	public void setSelfQueryOption(String selfQueryOption) {
		m_selfQueryOption = selfQueryOption;
	}

	public String getQueryIP() {
		return m_queryIP;
	}

	public void setQueryIP(String queryIP) {
		m_queryIP = queryIP;
	}

	public String getQueryType() {
		return m_queryType;
	}

	public void setQueryType(String queryType) {
		m_queryType = queryType;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public List<String> getGraphTypes() {
		return Arrays.asList("URL", "SQL", "CALL", "Error", "URL-Error", "Long-URL", "Long-SQL");
	}

	public void setGraphTypes(List<String> graphTypes) {
		m_graphTypes = graphTypes;
	}

	public String getGraph() {
		return m_graph;
	}

	public void setGraph(String graph) {
		m_graph = graph;
	}

	public String getGraphType() {
		return m_graphType;
	}

	public void setGraphType(String graphType) {
		m_graphType = graphType;
	}

	public Date getCreatTime() {
		return new Date();
	}

	public Date getDate() {
		return new Date();
	}

	public String getDomain() {
		return "Cat";
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public String getIp() {
		return m_ip;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public List<String> getDomains() {
		List<String> result = new ArrayList<String>();
		result.add("MobileApi");
		result.add("Cat");
		return result;
	}

	public void setDomains(List<String> domains) {
		m_domains = domains;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
