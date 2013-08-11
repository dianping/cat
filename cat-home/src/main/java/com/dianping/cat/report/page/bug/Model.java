package com.dianping.cat.report.page.bug;

import java.util.ArrayList;
import java.util.Collection;

import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context>  {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_REPORT;
	}

	@Override
   public String getDomain() {
	   return CatString.CAT;
   }

	@Override
   public Collection<String> getDomains() {
		return new ArrayList<String>();
	}
}
