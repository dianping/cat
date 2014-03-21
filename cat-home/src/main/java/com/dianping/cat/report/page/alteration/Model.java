package com.dianping.cat.report.page.alteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.alteration.Handler.AltBarrel;

public class Model extends AbstractReportModel<Action, Context> {

	private String m_status;

	private List<AltBarrel> m_barrels;

	public String getStatus() {
		return m_status;
	}

	public void setStatus(String status) {
		m_status = status;
	}

	public List<AltBarrel> getBarrels() {
		return m_barrels;
	}

	public void setBarrels(List<AltBarrel> barrels) {
		m_barrels = barrels;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	@Override
   public String getDomain() {
	   return Constants.CAT;
   }
}
