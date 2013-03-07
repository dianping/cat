package com.dianping.cat.report.page.query;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.query.Handler.TransactionQueryItem;

public class Model extends AbstractReportModel<Action, Context> {

	private List<TransactionQueryItem> m_transactionItems;
	
	public List<TransactionQueryItem> getTransactionItems() {
		return m_transactionItems;
	}

	public void setTransactionItems(List<TransactionQueryItem> transactionItems) {
		m_transactionItems = transactionItems;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
   public String getDomain() {
	   return "Cat";
   }

	@Override
   public Collection<String> getDomains() {
	   return Collections.emptyList();
   }
}
