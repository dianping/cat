package com.dianping.cat.consumer.core.aggregation;

import java.text.ParseException;

public abstract class Format {

	private String m_pattern;
	
	public abstract String parse(String input) throws ParseException;

	public String getPattern() {
		return m_pattern;
	}

	public void setPattern(String pattern) {
		this.m_pattern = pattern;
	}
	
	
	
}
