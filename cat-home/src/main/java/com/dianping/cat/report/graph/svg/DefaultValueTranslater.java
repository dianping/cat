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
package com.dianping.cat.report.graph.svg;

import org.unidal.lookup.annotation.Named;

@Named(type = ValueTranslater.class)
public class DefaultValueTranslater implements ValueTranslater {
	@Override
	public double getMaxValue(double[] values) {
		double min = Integer.MAX_VALUE;
		double max = Integer.MIN_VALUE;
		int len = values.length;

		for (int i = 0; i < len; i++) {
			double value = values[i];

			if (value < min) {
				min = value;
			}

			if (value > max) {
				max = value;
			}
		}

		double maxLog = Math.log10(max);
		double maxValue = Math.pow(10, Math.ceil(maxLog));

		if (max > 0) {
			while (maxValue >= max * 2) {
				maxValue = maxValue / 2;
			}
		}

		return maxValue;
	}

	@Override
	public int[] translate(int height, double maxValue, double[] values) {
		int len = values.length;
		int[] result = new int[len];

		for (int i = 0; i < len; i++) {
			double value = values[i];
			double temp = value * height / maxValue;

			if (temp > 0 && temp < 1) {
				temp = 1;
			}
			result[i] = (int) temp;
		}

		return result;
	}
}
