package com.dianping.cat.abtest.spi.internal.conditions;

import com.dianping.cat.abtest.model.entity.Condition;

public class URLCondition extends AbstractABTestCondition implements ABTestCondition {
	public static final String ID = "url";

	@Override
	public boolean accept(Condition condition) {
		String url = condition.getText();

		if (url == null || url.length() == 0) {
			return true;
		}

		String actual = m_request.getRequestURL().toString();
		int pos = url.indexOf('*');
		String subUrl = url.substring(0, pos);

		ABTestComparator comparator = ABTestComparator.getByValue(condition.getComparator(),
		      ABTestComparator.EQUAL_INSENS);
		switch (comparator) {
		case EQUAL_INSENS:
			return url.equalsIgnoreCase(actual);
		case EQUAL_SENS:
			return url.equals(actual);
		case NOT_EQUAL_INSENS:
			return !url.equalsIgnoreCase(actual);
		case NOT_EQUAL_SENS:
			return !url.equals(actual);
		case MARCHES_INSENS:
			return actual.toLowerCase().startsWith(subUrl.toLowerCase());
		case MARCHES_SENS:
			return actual.startsWith(subUrl);
		case CONTAIN:
			return url.contains(actual);
		case NOT_CONTAIN:
			return !url.contains(actual);
		}

		return false;
	}
}
