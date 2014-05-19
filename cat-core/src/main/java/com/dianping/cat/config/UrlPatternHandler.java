package com.dianping.cat.config;

import java.util.List;

import com.dianping.cat.configuration.url.pattern.entity.PatternItem;

public interface UrlPatternHandler {

	/**
	 * register aggregation rule to handler
	 * 
	 * @param formats
	 *           page type and domain to rule List Map
	 */
	public void register(List<PatternItem> rules);

	/**
	 * parse input to output use aggregation rule
	 * 
	 * @param input
	 * @return string after parse
	 */
	public String handle(String input);

}
