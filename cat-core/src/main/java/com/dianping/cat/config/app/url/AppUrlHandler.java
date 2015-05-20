package com.dianping.cat.config.app.url;

import java.util.List;

import com.dianping.cat.configuration.app.url.entity.Rule;

public interface AppUrlHandler {

	public void register(List<Rule> rules);

	public String handle(int type, String url);

}
