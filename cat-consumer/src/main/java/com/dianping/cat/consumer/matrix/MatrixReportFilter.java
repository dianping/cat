package com.dianping.cat.consumer.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultXmlBuilder;

public class MatrixReportFilter extends DefaultXmlBuilder {

	private int m_maxSize = 400;

	private static final String OTHERS = "OTHERS";

	public void setMaxSize(int maxSize) {
		m_maxSize = maxSize;
	}

	@Override
	public void visitMatrixReport(MatrixReport matrixReport) {
		Map<String, Matrix> matrixs = matrixReport.getMatrixs();
		Collection<Matrix> matrix = matrixs.values();
		int size = matrix.size();

		if (size > m_maxSize) {
			List<Matrix> matrixList = new ArrayList<Matrix>(matrix);
			Collections.sort(matrixList, new MeatricCompartor());

			matrixs.clear();
			for (int i = 0; i < m_maxSize; i++) {
				Matrix temp = matrixList.get(i);
				matrixs.put(temp.getName(), temp);
			}

			Matrix value = new Matrix(OTHERS);
			for (int i = m_maxSize; i < size; i++) {
				Matrix item = matrixList.get(i);

				value.setType(item.getType());
				value.setCount(item.getCount() + value.getCount());
			}
			matrixs.put(OTHERS, value);
		}

		super.visitMatrixReport(matrixReport);
	}

	public static class MeatricCompartor implements Comparator<Matrix> {

		@Override
		public int compare(Matrix o1, Matrix o2) {
			return o2.getCount() - o1.getCount();
		}
	}

}