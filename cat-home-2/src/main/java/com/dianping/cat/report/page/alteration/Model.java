package com.dianping.cat.report.page.alteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dianping.cat.Constants;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {

	private String m_insertResult;

	private List<Alteration> m_alterations;
	
	public Model(Context ctx) {
		super(ctx);
	}

	public List<Alteration> getAlterations() {
		return m_alterations;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
   public String getDomain() {
	   return Constants.CAT;
   }

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public String getInsertResult() {
		return m_insertResult;
	}

	public void setAlterations(List<Alteration> alterations) {
		m_alterations = alterations;
	}

	public void setInsertResult(String insertResult) {
		m_insertResult = insertResult;
	}

}
