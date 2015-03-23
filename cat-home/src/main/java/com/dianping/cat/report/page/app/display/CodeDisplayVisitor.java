package com.dianping.cat.report.page.app.display;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.home.app.entity.Code;
import com.dianping.cat.home.app.entity.Command;
import com.dianping.cat.home.app.transform.BaseVisitor;

public class CodeDisplayVisitor extends BaseVisitor {

	private AppReport m_appReport;

	private String m_currentCommand;

	private int[] m_distributions = new int[20];

	public CodeDisplayVisitor() {
		init();
	}

	public AppReport getReport() {
		return m_appReport;
	}

	private void init() {
		for (int i = 1000; i >= 100; i -= 100) {
			m_distributions[10 - i / 100] = i;
		}
		for (int i = 10; i < 20; i++) {
			m_distributions[i] = -m_distributions[i - 10];
		}
	}

	@Override
	public void visitAppReport(AppReport appReport) {
		m_appReport = new AppReport(appReport.getId());
		m_appReport.setStartTime(appReport.getStartTime()).setEndTime(appReport.getEndTime());

		super.visitAppReport(appReport);
	}

	@Override
	public void visitCode(Code code) {
		String dist = queryCodeDistribution(Integer.valueOf(code.getId()));

		buildDistributionInfo(code, dist);
		mergeCode(code, code.getId());

		super.visitCode(code);
	}

	private void buildDistributionInfo(Code code, String id) {
		Code c = m_appReport.findOrCreateCommand(m_currentCommand).findOrCreateCode(id);

		c.incCount(code.getCount()).incErrors(code.getErrors()).incSum(code.getSum());
		long count = c.getCount();

		if (count > 0) {
			c.setAvg(c.getSum() / count);
			c.setSuccessRatio(100.0 - c.getErrors() * 100.0 / count);
		}
		String title = c.getTitle();

		if (StringUtils.isEmpty(title)) {
			title = "";
		}
		StringBuilder sb = new StringBuilder(title);
		sb.append(code.getId() + "=" + code.getCount() + "; ");

		c.setTitle(sb.toString());
	}

	private void mergeCode(Code code, String id) {
		Code c = m_appReport.findOrCreateCommand(m_currentCommand).findOrCreateCode(id);

		c.setTitle(code.getTitle());
		c.incCount(code.getCount()).incErrors(code.getErrors()).incSum(code.getSum());

		long count = c.getCount();
		if (count > 0) {
			c.setAvg(c.getSum() / count);
			c.setSuccessRatio(100.0 - c.getErrors() * 100.0 / count);
		}
	}

	private void mergeCommand(Command command) {
		String id = command.getId();
		Command c = m_appReport.findOrCreateCommand(id);

		if (Constants.ALL.equals(id)) {
			c.setDomain(Constants.ALL);
			c.setTitle(Constants.ALL);
		} else {
			c.setDomain(command.getDomain());
			c.setTitle(command.getTitle());
		}
		c.incCount(command.getCount()).incSum(command.getSum()).incErrors(command.getErrors())
		      .incRequestSum(command.getRequestSum()).incResponseSum(command.getResponseSum());

		long count = c.getCount();
		if (count > 0) {
			c.setAvg(command.getSum() / count);
			c.setSuccessRatio(100.0 - c.getErrors() * 100.0 / count);
			c.setRequestAvg(c.getRequestSum() * 1.0 / count);
			c.setResponseAvg(c.getResponseSum() * 1.0 / count);
		}
	}

	public String queryCodeDistribution(int code) {
		if (code >= 0 && code < 100) {
			return "0XX";
		} else if (code > -100 && code < 0) {
			return "-0XX";
		} else {
			for (int i = 0; i < m_distributions.length; i++) {
				if (code / m_distributions[i] >= 1) {
					return convertLable(m_distributions[i]);
				}
			}
		}
		return null;
	}

	private String convertLable(int i) {
		String code = String.valueOf(i);

		return code.replaceAll("0", "X");
	}

	@Override
	public void visitCommand(Command command) {
		m_currentCommand = command.getId();
		mergeCommand(command);

		super.visitCommand(command);
	}

}
