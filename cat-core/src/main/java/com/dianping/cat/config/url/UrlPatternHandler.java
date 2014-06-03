package com.dianping.cat.config.url;

import java.util.Collection;

import com.dianping.cat.configuration.url.pattern.entity.PatternItem;

public interface UrlPatternHandler {

	/**
	 * register url rule to handler
	 */
	public void register(Collection<PatternItem> rules);

	/**
	 * parse input to output use aggregation rule
	 * 
	 * @param input
	 * @return string after parse
	 */
	public String handle(String input);

}
