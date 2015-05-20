package com.dianping.cat.config.app.command;

import java.util.List;

import com.dianping.cat.configuration.app.command.entity.Rule;

public interface CommandFormatHandler {

	public void register(List<Rule> rules);

	public String handle(int type, String url);

}
