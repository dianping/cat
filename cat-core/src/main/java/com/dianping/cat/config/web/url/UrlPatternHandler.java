package com.dianping.cat.config.web.url;

import java.util.Collection;

import com.dianping.cat.configuration.web.url.entity.PatternItem;

public interface UrlPatternHandler {

	public void register(Collection<PatternItem> rules);

	public String handle(String input);

}
