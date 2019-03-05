/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	private static final String OTHERS = "OTHERS";

	private int m_maxSize = 400;

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