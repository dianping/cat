package com.dianping.cat.consumer.cross;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Type;
import com.dianping.cat.consumer.cross.model.transform.DefaultMerger;

public class CrossReportMerger extends DefaultMerger {

	public CrossReportMerger(CrossReport crossReport) {
		super(crossReport);
	}

	@Override
	protected void mergeName(Name old, Name other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setSum(old.getSum() + other.getSum());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
		}
	}

	@Override
	protected void mergeType(Type old, Type other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setSum(old.getSum() + other.getSum());

		if (old.getId() == null) {
			old.setId(other.getId());
		}
		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
		}
	}

	@Override
	public void visitCrossReport(CrossReport crossReport) {
		super.visitCrossReport(crossReport);
		CrossReport report = getCrossReport();
		report.getDomainNames().addAll(crossReport.getDomainNames());
		report.getIps().addAll(crossReport.getIps());
	}
}
