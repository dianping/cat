package com.dianping.cat.consumer.transaction;

import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class StatisticsComputer extends BaseVisitor {
	double std(long count, double avg, double sum2, double max) {
		double value = sum2 / count - avg * avg;

		if (value <= 0 || count <= 1) {
			return 0;
		} else if (count == 2) {
			return max - avg;
		} else {
			return Math.sqrt(value);
		}
	}

	@Override
	public void visitName(TransactionName name) {
		super.visitName(name);

		long count = name.getTotalCount();

		if (count > 0) {
			long failCount = name.getFailCount();
			double avg = name.getSum() / count;
			double std = std(count, avg, name.getSum2(), name.getMax());
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
			double std = std(count, avg, type.getSum2(), type.getMax());
			double failPercent = 100.0 * failCount / count;

			type.setFailPercent(failPercent);
			type.setAvg(avg);
			type.setStd(std);
		}
	}
}