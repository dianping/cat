package com.dianping.cat.consumer.transaction;

import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class MeanSquareDeviationComputer extends BaseVisitor {
	@Override
	public void visitName(TransactionName name) {
		super.visitName(name);

		long count = name.getTotalCount();

		if (count > 0) {
			long failCount = name.getFailCount();
			double avg = name.getSum() / count;
			double std = std(count, avg, name.getSum2());
			double failPercent = 100.0 * failCount / count;

			name.setFailPercent(failPercent);
			name.setAvg(avg);
			name.setStd(std);
		}
	}

	@Override
	public void visitRange(Range range) {
		if (range.getCount() > 0) {
			range.setAvg(range.getSum() / range.getCount());
		}
	}

	@Override
	public void visitType(TransactionType type) {
		super.visitType(type);

		long count = type.getTotalCount();

		if (count > 0) {
			long failCount = type.getFailCount();
			double avg = type.getSum() / count;
			double std = std(count, avg, type.getSum2());
			double failPercent = 100.0 * failCount / count;

			type.setFailPercent(failPercent);
			type.setAvg(avg);
			type.setStd(std);
		}
	}

	double std(long count, double avg, double sum2) {
		return Math.sqrt(sum2 / count - avg * avg);
	}
}