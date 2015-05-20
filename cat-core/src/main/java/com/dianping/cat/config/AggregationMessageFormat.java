package com.dianping.cat.config;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AggregationMessageFormat {

	private List<String> m_formatTokens = new ArrayList<String>();

	private MessageFormat m_messageFormat;

	public AggregationMessageFormat(String pattern) {
		m_messageFormat = new MessageFormat(build(pattern));
	}

	private String build(String pattern) {
		int index = 0;
		Pattern p = Pattern.compile("\\{(.*?)\\}");
		Matcher matcher = p.matcher(pattern);
		StringBuffer output = new StringBuffer();
		
		while (matcher.find()) {
			m_formatTokens.add(matcher.group(1).trim());
			matcher.appendReplacement(output, "{" + index + "}");
			if (index < 9) {
				index++;
			}
		}
		matcher.appendTail(output);
		return output.toString();
	}

	public List<String> getFormatTokens() {
		return m_formatTokens;
	}

	public MessageFormat getMessageFormat() {
		return (MessageFormat) m_messageFormat.clone();
	}

}
