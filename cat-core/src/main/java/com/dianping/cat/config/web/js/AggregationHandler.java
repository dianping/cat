package com.dianping.cat.config.web.js;

import java.util.List;

import com.dianping.cat.configuration.web.js.entity.AggregationRule;

public interface AggregationHandler {

	/**
	 * register aggregation rule to handler
	 * 
	 * @param formats
	 *           page type and domain to rule List Map
	 */
	public void register(List<AggregationRule> rules);
	
	/**
	 * parse input to output use aggregation rule
	 * 
	 * @param type
	 *           page type, like transaction,problem。。
	 * @param domain
	 * @param input
	 * @return string after parse
	 */
	public String handle(int type, String domain, String input);

}
