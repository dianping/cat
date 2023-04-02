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

	private List<AlertMachine> specMachines;

	private List<AlertMachine> totalMachines;

	private Double specWarnLimit;

	private Double specErrorLimit;

	private Double totalWarnLimit;

	private Double totalErrorLimit;

	public GroupAlertException(List<AlertException> specExceptions, List<AlertException> totalExceptions,
							   List<AlertMachine> specMachines, List<AlertMachine> totalMachines,
							   Double specWarnLimit, Double specErrorLimit, Double totalWarnLimit, Double totalErrorLimit) {
		this.specExceptions = specExceptions;
		this.totalExceptions = totalExceptions;
		this.specMachines = specMachines;
		this.totalMachines = totalMachines;
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

	public List<AlertMachine> getSpecMachines() {
		return specMachines;
	}

	public List<AlertMachine> getTotalMachines() {
		return totalMachines;
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

	public String showSpecWarnLimit() {
		return AlertException.doubleToText(specWarnLimit);
	}

	public String showSpecErrorLimit() {
		return AlertException.doubleToText(specErrorLimit);
	}

	public String showTotalWarnLimit() {
		return AlertException.doubleToText(totalWarnLimit);
	}

	public String showTotalErrorLimit() {
		return AlertException.doubleToText(totalErrorLimit);
	}
}
