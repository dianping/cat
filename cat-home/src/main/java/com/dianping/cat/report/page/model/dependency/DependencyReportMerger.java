package com.dianping.cat.report.page.model.dependency;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.dependency.model.transform.DefaultMerger;

public class DependencyReportMerger extends DefaultMerger {

	public DependencyReportMerger(DependencyReport dependencyReport) {
		super(dependencyReport);
	}

	@Override
	protected void mergeDependency(Dependency old, Dependency dependency) {
		old.setType(dependency.getType());
		old.setTarget(dependency.getTarget());
		old.setTotalCount(old.getTotalCount() + dependency.getTotalCount());
		old.setErrorCount(old.getErrorCount() + dependency.getErrorCount());
		old.setSum(old.getSum() + dependency.getSum());
		if (old.getTotalCount() > 0) {
			old.setAvg(old.getSum() / old.getTotalCount());
		}
	}

	@Override
	protected void mergeIndex(Index old, Index index) {
		old.setTotalCount(old.getTotalCount() + index.getTotalCount());
		old.setErrorCount(old.getErrorCount() + index.getErrorCount());
		old.setSum(old.getSum() + index.getSum());
		if (old.getTotalCount() > 0) {
			old.setAvg(old.getSum() / old.getTotalCount());
		}
	}

	@Override
	protected void mergeSegment(Segment old, Segment segment) {
		old.setExceptionCount(old.getExceptionCount() + segment.getExceptionCount());
	}

	@Override
	public void visitDependencyReport(DependencyReport dependencyReport) {
		super.visitDependencyReport(dependencyReport);

		DependencyReport report = getDependencyReport();
		report.getDomainNames().addAll(dependencyReport.getDomainNames());
		report.setStartTime(dependencyReport.getStartTime());
		report.setEndTime(dependencyReport.getEndTime());
	}

}
