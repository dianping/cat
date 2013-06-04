package com.dianping.cat.system.page.aggregation;

import java.util.Collections;
import java.util.List;

import com.dainping.cat.consumer.core.dal.AggregationRule;
import com.dianping.cat.system.SystemPage;

import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<SystemPage, Action, Context> {
	private AggregationRule m_aggregationRule;
	
	private List<AggregationRule> m_aggregationRules;
	
	public AggregationRule getAggregationRule() {
		
		return m_aggregationRule;
	}

	public List<AggregationRule> getAggregationRules() {
		return m_aggregationRules;
	}

	public void setAggregationRule(AggregationRule m_aggregationRule) {
		this.m_aggregationRule = m_aggregationRule;
	}

	public void setAggregationRules(List<AggregationRule> m_aggregationRules) {
		this.m_aggregationRules = m_aggregationRules;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.ALL;
	}
	
	public String getDate() {
		return "";
	}

	public String getDomain() {
		return "";
	}

	public List<String> getDomains() {
		return Collections.emptyList();
	}

	public String getIpAddress() {
		return "";
	}

}
