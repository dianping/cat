package com.dianping.cat.report.page.app.display;

import com.dianping.cat.Constants;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.home.app.entity.Code;
import com.dianping.cat.home.app.entity.Command;
import com.dianping.cat.home.app.transform.BaseVisitor;

public class AppReportMerger extends BaseVisitor {

	private AppReport m_report;

	private String m_commandId;

	public AppReport getReport() {
		return m_report;
	}

	private void mergeCode(Code code, String id) {
		Code c = m_report.findOrCreateCommand(id).findOrCreateCode(code.getId());

		c.setTitle(code.getTitle());
		c.incCount(code.getCount()).incSum(code.getSum()).incErrors(code.getErrors());

		long count = c.getCount();
		if (count > 0) {
			c.setAvg(c.getSum() / count);
			c.setSuccessRatio(100.0 - c.getErrors() * 100.0 / count);
		}
	}

	private void mergeCommand(Command command, String id) {
		Command c = m_report.findOrCreateCommand(id);

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
			c.setAvg(c.getSum() / count);
			c.setSuccessRatio(100.0 - c.getErrors() * 100.0 / count);
			c.setRequestAvg(c.getRequestSum() * 1.0 / count);
			c.setResponseAvg(c.getResponseSum() * 1.0 / count);
		}
	}

	@Override
	public void visitAppReport(AppReport appReport) {
		m_report = new AppReport(appReport.getId());
		m_report.setStartTime(appReport.getStartTime()).setEndTime(appReport.getEndTime());

		super.visitAppReport(appReport);
	}

	@Override
	public void visitCode(Code code) {
		mergeCode(code, Constants.ALL);
		mergeCode(code, m_commandId);
		super.visitCode(code);
	}

	@Override
	public void visitCommand(Command command) {
		m_commandId = command.getId();

		mergeCommand(command, Constants.ALL);
		mergeCommand(command, String.valueOf(command.getId()));

		super.visitCommand(command);
	}

}
