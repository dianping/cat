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
	}
}
