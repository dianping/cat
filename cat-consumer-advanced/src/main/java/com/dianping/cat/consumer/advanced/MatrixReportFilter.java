package com.dianping.cat.consumer.advanced;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultXmlBuilder;

public class MatrixReportFilter extends DefaultXmlBuilder {
	private String m_domain;

	private int m_maxItems = 200;

	private static final String OTHERS = "OTHERS";

	@Override
	public void visitMatrixReport(MatrixReport matrixReport) {
		m_domain = matrixReport.getDomain();
		Map<String, Matrix> matrixs = matrixReport.getMatrixs();

		long total = 0;
		for (Matrix matrix : matrixs.values()) {
			total = total + matrix.getCount();
		}

		int value = (int) (total / 10000);
		String urlSample = null;
		value = Math.min(value, 5);

		if (!m_domain.equals("Cat") && (value > 0)) {
			int totalCount = 0;
			Collection<Matrix> matrix = matrixs.values();
			List<String> removeUrls = new ArrayList<String>();

			if (matrix.size() > m_maxItems) {
				for (Matrix temp : matrix) {
					if (temp.getType().equals("URL") && temp.getCount() < 5) {
						removeUrls.add(temp.getName());
						totalCount += temp.getCount();
						if (urlSample == null) {
							urlSample = temp.getUrl();
						}
					}
				}
				for (String url : removeUrls) {
					matrixs.remove(url);
				}

				if (totalCount > 0) {
					Matrix other = matrixReport.findOrCreateMatrix(OTHERS);

					other.setUrl(urlSample);
					other.setType(OTHERS);
					other.setCount(totalCount + other.getCount());
				}
			}
		}
		super.visitMatrixReport(matrixReport);
	}
}