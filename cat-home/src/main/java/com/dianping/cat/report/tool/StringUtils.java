package com.dianping.cat.report.tool;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

	public static List<String> getListFromPage(String pageResult, String start, String end) {
		String resultStr = getStringFromPage(pageResult, start, end);
		List<String> result = new ArrayList<String>();
		String[] temp = resultStr.split("\t");
		for (String str : temp) {
			result.add(str);
		}
		return result;
	}

	public static String getStringFromPage(String pageResult, String start, String end) {
		String result = "";
		int startIndex = pageResult.indexOf(start) + start.length();
		int endIndex = pageResult.indexOf(end);
		result = pageResult.substring(startIndex, endIndex);
		return result;
	}
}
