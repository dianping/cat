package com.dianping.cat.consumer.core.aggregation;

import java.util.List;
import java.util.Map;

import com.dainping.cat.consumer.core.dal.AggregationRule;

public interface AggregationHandler {
	
	/**
	 * register aggregation rule to handler
	 * @param formats page type and domain to rule List Map
	 */
	public void register(Map<Integer, Map<String, List<AggregationRule>>>  formats);
	
	/**
	 * parse input to output use aggregation rule
	 * @param type page type, like transaction,problem。。
	 * @param domain
	 * @param input
	 * @return string after parse
	 */
	public String handle(int type, String domain, String input);

}
