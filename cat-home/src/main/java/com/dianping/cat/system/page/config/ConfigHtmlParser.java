package com.dianping.cat.system.page.config;

public class ConfigHtmlParser {

	public String parse(String content) {
		content = content.replaceAll("<", "&lt;");
		content = content.replaceAll(">", "&gt;");

		return content;
	}

}
