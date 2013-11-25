package com.dianping.cat.system.page.abtest.conditions;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.abtest.model.entity.Condition;

public class URLScriptProvider implements ScriptProvider {

	private String m_actual = "actual";

	public static final int EQUALS_CASE_INSENS = 1;

	public static final int NOT_EQUALS_CASE_INSENS = 2;

	public static final int EQUALS_CASE_SENS = 3;

	public static final int NOT_EQUALS_CASE_SENS = 4;

	public static final int MARCH_CASE_INSENS = 5;

	public static final int MARCH_CASE_SENS = 6;

	public static final int CONTAIN = 7;

	public static final int NOT_CONTAIN = 8;

	private String contain(String expectedUrl) {
		return String.format("%s.indexOf(\"%s\") > -1", m_actual, expectedUrl);
	}

	private String equalsByCaseInsens(String expectedUrl) {
		return String.format("%s.equalsIgnoreCase(\"%s\")", m_actual, expectedUrl);
	}

	private String equalsByCaseSens(String expectedUrl) {
		return String.format("%s.equals(\"%s\")", m_actual, expectedUrl);
	}

	@Override
	public String getScript(Condition condition) {
		String expectedUrl = condition.getText();

		switch (condition.getComparator()) {
		case EQUALS_CASE_INSENS:
			return equalsByCaseInsens(expectedUrl);
		case NOT_EQUALS_CASE_INSENS:
			return notEqualsByCaseInsens(expectedUrl);
		case EQUALS_CASE_SENS:
			return equalsByCaseSens(expectedUrl);
		case NOT_EQUALS_CASE_SENS:
			return notEqualsByCaseSens(expectedUrl);
		case MARCH_CASE_INSENS:
			return marcherByCaseInsens(expectedUrl);
		case MARCH_CASE_SENS:
			return marcherByCaseSens(expectedUrl);
		case CONTAIN:
			return contain(expectedUrl);
		case NOT_CONTAIN:
			return notContain(expectedUrl);
		}

		return "false";
	}

	private String marcherByCaseInsens(String expectedUrl) {
		int pos = expectedUrl.indexOf('*');
		String subUrl = "";

		if (pos > 0) {
			if (expectedUrl.charAt(pos - 1) == '/') {
				subUrl = expectedUrl.substring(0, pos - 1);
			} else {
				subUrl = expectedUrl.substring(0, pos);
			}

			return String.format("%s.toLowerCase().startsWith(\"%s\".toLowerCase())", m_actual, subUrl);
		} else {
			return "false";
		}

	}

	private String marcherByCaseSens(String expectedUrl) {
		int pos = expectedUrl.indexOf('*');
		String subUrl = "";

		if (pos > 0) {
			if (expectedUrl.charAt(pos - 1) == '/') {
				subUrl = expectedUrl.substring(0, pos - 1);
			} else {
				subUrl = expectedUrl.substring(0, pos);
			}

			return String.format("%s.startsWith(\"%s\")", m_actual, subUrl);
		} else {
			return "false";
		}
	}

	private String notContain(String expectedUrl) {
		return String.format("%s.indexOf(\"%s\") == -1", m_actual, expectedUrl);
	}

	private String notEqualsByCaseInsens(String expectedUrl) {
		return String.format("!%s.equalsIgnoreCase(\"%s\")", m_actual, expectedUrl);
	}

	private String notEqualsByCaseSens(String expectedUrl) {
		return String.format("!%s.equals(\"%s\")", m_actual, expectedUrl);
	}

	@Override
	public Map<Integer, Object> options() {
		Map<Integer, Object> actions = new HashMap<Integer, Object>();

		actions.put(EQUALS_CASE_INSENS, "Is equals to (case insens.)");
		actions.put(NOT_EQUALS_CASE_INSENS, "Is not equals to (case insens.)");
		actions.put(EQUALS_CASE_SENS, "Is equals to (case sens.)");
		actions.put(NOT_EQUALS_CASE_SENS, "Is not equals to (case sens.)");
		actions.put(MARCH_CASE_INSENS, "Marches Regex (case insens.)");
		actions.put(MARCH_CASE_SENS, "Marches Regex (case sens.)");
		actions.put(CONTAIN, "contains");
		actions.put(NOT_CONTAIN, "does not contain");

		return actions;
	}
}
