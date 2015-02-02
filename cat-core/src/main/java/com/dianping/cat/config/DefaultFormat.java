package com.dianping.cat.config;

import java.text.ParseException;

public class DefaultFormat extends Format {

	public static void main(String[] str) {
	}

	@Override
	public String parse(String input) throws ParseException {
		String pattern = getPattern();
		String item = "";
		String describe = "";
		int index = pattern.indexOf(":");
		
		if (index != -1 && pattern.length() > index + 1) {
			item = pattern.substring(0, index).trim();
			describe = pattern.substring(index + 1).trim();
		}
		if (!describe.isEmpty()) {
			int length = 1;
			try {
				length = Integer.parseInt(describe);
			} catch (NumberFormatException e) {
				throw new ParseException(pattern + "is illegal", 0);
			}
			if (input.length() != length) {
				throw new ParseException("not match " + pattern, 0);
			}
		}
		if (pattern.equals("*")) {
			return input;
		} else if (item.equals("md5")) {
			char[] charArray = input.toCharArray();
			for (Character ch : charArray) {
				if (!Character.isDigit(ch) && !Character.isLowerCase(ch)) {
					return input;
				}
			}
		} else if (item.equals("number")) {
			char[] charArray = input.toCharArray();
			for (Character ch : charArray) {
				if (!Character.isDigit(ch)) {
					return input;
				}
			}
		}
		return ("{" + pattern + "}");
	}
}
