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

	private Double specWarnLimit;

	private Double specErrorLimit;

	private Double totalWarnLimit;

	private Double totalErrorLimit;

	public GroupAlertException(List<AlertException> specExceptions, List<AlertException> totalExceptions, Double specWarnLimit, Double specErrorLimit, Double totalWarnLimit, Double totalErrorLimit) {
		this.specExceptions = specExceptions;
		this.totalExceptions = totalExceptions;
		this.specWarnLimit = specWarnLimit;
		this.specErrorLimit = specErrorLimit;
		this.totalWarnLimit = totalWarnLimit;
		this.totalErrorLimit = totalErrorLimit;
	}

	public List<AlertException> getSpecExceptions() {
		return specExceptions;
	}

	public List<AlertException> getTotalExceptions() {
		return totalExceptions;
	}

	public Double getSpecWarnLimit() {
		return specWarnLimit;
	}

	public Double getSpecErrorLimit() {
		return specErrorLimit;
	}

	public Double getTotalWarnLimit() {
		return totalWarnLimit;
	}

	public Double getTotalErrorLimit() {
		return totalErrorLimit;
	}
}
