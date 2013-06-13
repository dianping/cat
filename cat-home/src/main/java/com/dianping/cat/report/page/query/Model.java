package com.dianping.cat.report.page.query;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.query.display.EventQueryItem;
import com.dianping.cat.report.page.query.display.ProblemQueryItem;
import com.dianping.cat.report.page.query.display.TransactionQueryItem;

public class Model extends AbstractReportModel<Action, Context> {

	private List<TransactionQueryItem> m_transactionItems;
	
	private List<EventQueryItem> m_eventItems;
	
	private List<ProblemQueryItem> m_problemItems;
	
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

	public List<EventQueryItem> getEventItems() {
		return m_eventItems;
	}

	public List<ProblemQueryItem> getProblemItems() {
		return m_problemItems;
	}

	public List<TransactionQueryItem> getTransactionItems() {
		return m_transactionItems;
	}

	public void setEventItems(List<EventQueryItem> eventItems) {
		m_eventItems = eventItems;
	}

	public void setProblemItems(List<ProblemQueryItem> problemItems) {
		m_problemItems = problemItems;
	}

	public void setTransactionItems(List<TransactionQueryItem> transactionItems) {
		m_transactionItems = transactionItems;
	}
	
}
