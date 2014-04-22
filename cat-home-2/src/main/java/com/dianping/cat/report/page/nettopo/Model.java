package com.dianping.cat.report.page.nettopo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.unidal.tuple.Pair;

import com.dianping.cat.report.page.AbstractReportModel;


public class Model extends AbstractReportModel<Action, Context> {
	private String m_domain;
	
	private ArrayList<Pair<String, String>> netData;
	
	public ArrayList<Pair<String, String>> getNetData() {
		return netData;
	}

	public void setNetData(ArrayList<Pair<String, String>> netData) {
		this.netData = netData;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	//@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
	
	//@Override
	public String getDomain() {
		return m_domain;
	}
	
	public void setDomain(String domain) {
		m_domain = domain;
	}

	@Override
	public Collection<String> getDomains() {
		return Collections.emptySet();
	}
}
