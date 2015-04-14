package com.dianping.cat.config.content;

import org.unidal.helper.Files;

import com.dianping.cat.Cat;

public class DefaultContentFetcher implements ContentFetcher {
	private final String PATH = "/config/";

	@Override
	public String getConfigContent(String configName) {
		String path = PATH + configName + ".xml";
		String content = "";

		try {
			content = Files.forIO().readFrom(this.getClass().getResourceAsStream(path), "utf-8");
		} catch (Exception e) {
			Cat.logError(e);
		}
		return content;
	}
}
