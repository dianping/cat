package com.dianping.cat.consumer.core.aggregation;

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

	/**
	 * build message format by user's aggregation rule
	 * 
	 * @param pattern
	 * @return
	 */
	private String build(String pattern) {
		Pattern p = Pattern.compile("\\{(.*?)\\}");
		Matcher matcher = p.matcher(pattern);
		int index = 0;
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

	/**
	 * message format is not thread safe, so return a clone
	 * 
	 * @return message format clone
	 */
	public MessageFormat getMessageFormat() {
		return (MessageFormat) m_messageFormat.clone();
	}

}
