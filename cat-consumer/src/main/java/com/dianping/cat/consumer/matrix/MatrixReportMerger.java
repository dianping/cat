package com.dianping.cat.consumer.matrix;

import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;
import com.dianping.cat.consumer.matrix.model.transform.DefaultMerger;

public class MatrixReportMerger extends DefaultMerger {

	public MatrixReportMerger(MatrixReport matrixReport) {
		super(matrixReport);
	}

	@Override
	protected void mergeMatrix(Matrix old, Matrix matrix) {
		old.setCount(old.getCount() + matrix.getCount());
		old.setTotalTime(old.getTotalTime() + matrix.getTotalTime());
		if (old.getType() == null) {
			old.setType(matrix.getType());
		}
		if (old.getUrl() == null) {
			old.setUrl(matrix.getUrl());
		}
	}

	@Override
	protected void mergeRatio(Ratio old, Ratio ratio) {
		old.setTotalCount(old.getTotalCount() + ratio.getTotalCount());
		old.setTotalTime(old.getTotalTime() + ratio.getTotalTime());
		if (old.getMin() == 0) {
			old.setMin(ratio.getMin());
		}
		if (ratio.getMin() < old.getMin()) {
			old.setMin(ratio.getMin());
		}
		if (ratio.getMax() > old.getMax()) {
			old.setMax(ratio.getMax());
			old.setUrl(ratio.getUrl());
		}
	}

	@Override
	public void visitMatrixReport(MatrixReport matrixReport) {
		super.visitMatrixReport(matrixReport);
		getMatrixReport().getDomainNames().addAll(matrixReport.getDomainNames());
	}
}
