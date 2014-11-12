package com.dianping.cat.config.content;

import java.io.IOException;

import org.unidal.helper.Files;

import com.dianping.cat.Cat;

public class DefaultContentGetter implements ContentGetter {
	private final String PATH = "/config/backup/";

	@Override
	public String getConfigContent(String configName) {
		String path = PATH + configName + ".xml";
		String content = "";

		try {
			content = Files.forIO().readFrom(this.getClass().getResourceAsStream(path), "utf-8");
		} catch (IOException e) {
			Cat.logError(e);
		}
		return content;
	}
}
