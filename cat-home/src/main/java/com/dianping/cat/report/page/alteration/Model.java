package com.dianping.cat.report.page.alteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.alteration.Handler.AltBarrel;

public class Model extends AbstractReportModel<Action, Context> {

	private String m_insertResult;

	private Map<Long, AltBarrel> m_barrels;
	
	public Model(Context ctx) {
		super(ctx);
	}

	public Map<Long, AltBarrel> getBarrels() {
		return m_barrels;
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

	public void setBarrels(Map<Long, AltBarrel> barrels) {
		m_barrels = barrels;
	}

	public void setInsertResult(String insertResult) {
		m_insertResult = insertResult;
	}

}
