package com.dianping.cat.report.alert.exception;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class GroupAlertException {

	private List<AlertException> specExceptions;

	private List<AlertException> totalExceptions;

	public GroupAlertException(List<AlertException> specExceptions, List<AlertException> totalExceptions) {
		this.specExceptions = specExceptions;
		this.totalExceptions = totalExceptions;
	}

	public List<AlertException> getSpecExceptions() {
		return specExceptions;
	}

	public List<AlertException> getTotalExceptions() {
		return totalExceptions;
	}

}
