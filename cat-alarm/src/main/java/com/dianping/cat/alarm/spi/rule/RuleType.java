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
package com.dianping.cat.alarm.spi.rule;

import com.dianping.cat.Cat;
import org.unidal.tuple.Pair;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public enum RuleType {

	DecreasePercentage {
		private double[] buildDescPers(double[] values, double[] baselines) {
			int length = values.length;
			double[] descPers = new double[length];

			for (int i = 0; i < length; i++) {
				descPers[i] = (1 - values[i] / baselines[i]) * 100;
			}

			return descPers;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[基线值:").append(convertDoublesToString(baselines)).append("] ");
			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[下降比:").append(convertPercentsToString(buildDescPers(values, baselines))).append("]");
			sb.append("[下降百分比阈值: ").append(m_df.format(ruleValue)).append("% ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");
			sb.append("[下降比为实际值与基线值相比下降的百分比]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (baselines[i] <= 0 || values[i] / baselines[i] > (1 - ruleValue / 100)) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "DescPer";
		}

	},

	DecreaseValue {
		private double[] buildDescVals(double[] values, double[] baselines) {
			int length = values.length;
			double[] descVals = new double[length];

			for (int i = 0; i < length; i++) {
				descVals[i] = baselines[i] - values[i];
			}

			return descVals;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[基线值:").append(convertDoublesToString(baselines)).append("] ");
			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[下降值:").append(convertDoublesToString(buildDescVals(values, baselines))).append("]");
			sb.append("[下降阈值: ").append(convertDoubleToString(ruleValue)).append(" ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");
			sb.append("[下降值为实际值与基线值相比下降的值]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (baselines[i] - values[i] < ruleValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "DescVal";
		}
	},

	IncreasePercentage {
		private double[] buildAscPers(double[] values, double[] baselines) {
			int length = values.length;
			double[] ascPers = new double[length];

			for (int i = 0; i < length; i++) {
				ascPers[i] = (values[i] / baselines[i] - 1) * 100;
			}

			return ascPers;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[基线值:").append(convertDoublesToString(baselines)).append("] ");
			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[上升比:").append(convertPercentsToString(buildAscPers(values, baselines))).append("]");
			sb.append("[上升百分比阈值: ").append(m_df.format(ruleValue)).append("% ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");
			sb.append("[上升比为实际值与基线值相比上升的百分比]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (baselines[i] <= 0 || values[i] / baselines[i] < (1 + ruleValue / 100)) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "AscPer";
		}
	},

	IncreaseValue {
		private double[] buildAscVals(double[] values, double[] baselines) {
			int length = values.length;
			double[] ascVals = new double[length];

			for (int i = 0; i < length; i++) {
				ascVals[i] = values[i] - baselines[i];
			}

			return ascVals;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[基线值:").append(convertDoublesToString(baselines)).append("] ");
			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[上升值:").append(convertDoublesToString(buildAscVals(values, baselines))).append("]");
			sb.append("[上升阈值: ").append(convertDoubleToString(ruleValue)).append(" ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");
			sb.append("[上升值为实际值与基线值相比上升的值]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (values[i] - baselines[i] < ruleValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "AscVal";
		}
	},

	AbsoluteMaxValue {
		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[最大阈值: ").append(convertDoubleToString(ruleValue)).append(" ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (values[i] < ruleValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "MaxVal";
		}
	},

	AbsoluteMinValue {
		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[最小阈值: ").append(convertDoubleToString(ruleValue)).append(" ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (values[i] > ruleValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "MinVal";
		}
	},

	FluctuateIncreasePercentage {
		private double[] buildFlucAscPers(double[] values) {
			int length = values.length;
			double[] ascPers = new double[length - 1];
			double baseVal = values[length - 1];

			for (int i = 0; i <= length - 2; i++) {
				ascPers[i] = (baseVal / values[i] - 1) * 100;
			}

			return ascPers;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[波动上升百分比:").append(convertPercentsToString(buildFlucAscPers(values))).append("] ");
			sb.append("[波动上升百分比阈值: ").append(m_df.format(ruleValue)).append("% ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");
			sb.append("[波动上升百分比为以最后一分钟的数据为基准，前面每分钟的值比基准值上升的百分比]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			int length = values.length;

			if (length <= 1) {
				return new Pair<Boolean, String>(false, "");
			}

			double baseVal = values[length - 1];

			for (int i = 0; i <= length - 2; i++) {
				if (baseVal / values[i] - 1 < ruleValue / 100) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "FluAscPer";
		}
	},

	FluctuateDecreasePercentage {
		private double[] buildFlucDescPers(double[] values) {
			int length = values.length;
			double[] descPers = new double[length - 1];
			double baseVal = values[length - 1];

			for (int i = 0; i <= length - 2; i++) {
				descPers[i] = (1 - baseVal / values[i]) * 100;
			}

			return descPers;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[波动下降百分比:").append(convertPercentsToString(buildFlucDescPers(values))).append("] ");
			sb.append("[波动下降百分比阈值: ").append(m_df.format(ruleValue)).append("% ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");
			sb.append("[波动下降百分比为以最后一分钟的数据为基准，前面每分钟的值比基准值下降的百分比]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			int length = values.length;

			if (length <= 1) {
				return new Pair<Boolean, String>(false, "");
			}

			double baseVal = values[length - 1];

			for (int i = 0; i <= length - 2; i++) {
				if (1 - baseVal / values[i] < ruleValue / 100) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "FluDescPer";
		}
	},

	SumMaxValue {
		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[实际值总和:").append(convertDoubleToString(calSum(values))).append("] ");
			sb.append("[总和最大阈值: ").append(convertDoubleToString(ruleValue)).append(" ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			double totalVal = calSum(values);

			if (totalVal < ruleValue) {
				return new Pair<Boolean, String>(false, "");
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "SumMaxVal";
		}
	},

	SumMinValue {
		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[实际值总和:").append(convertDoubleToString(calSum(values))).append("] ");
			sb.append("[总和最小阈值: ").append(convertDoubleToString(ruleValue)).append(" ]");
			sb.append("[告警时间:").append(sdf.format(new Date())).append("]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			double ruleValue = parseStringToDouble(rawValue);
			double totalVal = calSum(values);

			if (totalVal > ruleValue) {
				return new Pair<Boolean, String>(false, "");
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "SumMinVal";
		}
	},

	UserDefine {
		private final String USER_DEFINED_FOLDER = Cat.getCatHome() + "user_defined_class/";

		private static final String USER_DEFINED_CLASS_NAME = "UserDefinedRule.java";

		private Map<String, MonitorRule> m_rules = new HashMap<String, MonitorRule>();

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			return null;
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue) {
			MonitorRule instance = m_rules.get(rawValue);

			if (instance == null) {
				try {
					Pair<File, File> files = generateClassFile(rawValue);
					File userDefinedFolder = files.getKey();
					File userDefinedClassFile = files.getValue();
					JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
					compiler.run(null, null, null, userDefinedClassFile.getPath());

					URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { userDefinedFolder.toURI().toURL() });
					Class<?> cls = Class.forName("UserDefinedRule", true, classLoader);
					instance = (MonitorRule) cls.newInstance();

					m_rules.put(rawValue, instance);
				} catch (Exception e) {
					Cat.logError("generate user defined rule error: " + rawValue, e);
					return new Pair<Boolean, String>(false, "");
				}
			}
			return instance.checkData(values, baselines);
		}

		private Pair<File, File> generateClassFile(String rawValue) throws IOException {
			File userDefinedFolder = new File(USER_DEFINED_FOLDER);
			if (!userDefinedFolder.exists() || userDefinedFolder.isFile()) {
				userDefinedFolder.mkdirs();
			}

			File userDefinedClassFile = new File(userDefinedFolder, USER_DEFINED_CLASS_NAME);
			if (!userDefinedClassFile.exists() || userDefinedClassFile.isDirectory()) {
				userDefinedClassFile.createNewFile();
			}

			OutputStream output = new FileOutputStream(userDefinedClassFile);
			try {
				output.write(rawValue.getBytes());
			} finally {
				output.close();
			}
			return new Pair<File, File>(userDefinedFolder, userDefinedClassFile);
		}

		@Override
		public String getId() {
			return "UserDefine";
		}
	};

	protected static final long MbS = 60 * 1024 * 1024;

	protected static final long GbS = MbS * 1024;

	static Map<String, RuleType> s_map = new LinkedHashMap<String, RuleType>();

	static {
		for (RuleType type : RuleType.values()) {
			s_map.put(type.getId(), type);
		}
	}

	protected DecimalFormat m_df = new DecimalFormat("0.###");

	public static RuleType getByTypeId(String typeId) {
		return s_map.get(typeId);
	}

	protected abstract String buildRuleMessage(double[] values, double[] baselines, double ruleValue);

	protected double calSum(double[] values) {
		double totalVal = 0;

		for (double value : values) {
			totalVal += value;
		}
		return totalVal;
	}

	protected String convertDoublesToString(double[] values) {
		StringBuilder builder = new StringBuilder();

		for (double value : values) {
			builder.append(convertDoubleToString(value)).append(" ");
		}

		return builder.toString();
	}

	protected String convertDoubleToString(double value) {
		if (value < MbS) {
			return m_df.format(value);
		} else if (value < GbS) {
			return m_df.format(value / MbS / 8) + "MB/s";
		} else {
			return m_df.format(value / GbS / 8) + "GB/s";
		}
	}

	protected String convertPercentsToString(double[] values) {
		StringBuilder builder = new StringBuilder();

		for (double value : values) {
			builder.append(m_df.format(value)).append("% ");
		}

		return builder.toString();
	}

	public abstract Pair<Boolean, String> executeRule(double[] values, double[] baselines, String rawValue);

	public abstract String getId();

	protected double parseStringToDouble(String text) {
		if (text.endsWith("Mb/s")) {
			double value = Double.parseDouble(text.replaceAll("Mb/s", ""));
			return value * 60 * 1024 * 1024;
		} else if (text.endsWith("Gb/s")) {
			double value = Double.parseDouble(text.replaceAll("Gb/s", ""));
			return value * 60 * 1024 * 1024 * 1024;
		} else if (text.endsWith("MB/s")) {
			double value = Double.parseDouble(text.replaceAll("MB/s", ""));
			return value * 60 * 1024 * 1024 * 8;
		} else if (text.endsWith("GB/s")) {
			double value = Double.parseDouble(text.replaceAll("GB/s", ""));
			return value * 60 * 1024 * 1024 * 1024 * 8;
		} else if (text.endsWith("Mb")) {
			double value = Double.parseDouble(text.replaceAll("Mb", ""));
			return value * 1024 * 1024;
		} else if (text.endsWith("Gb")) {
			double value = Double.parseDouble(text.replaceAll("Gb", ""));
			return value * 1024 * 1024 * 1024;
		} else if (text.endsWith("MB")) {
			double value = Double.parseDouble(text.replaceAll("MB", ""));
			return value * 1024 * 1024 * 8;
		} else if (text.endsWith("GB")) {
			double value = Double.parseDouble(text.replaceAll("GB", ""));
			return value * 1024 * 1024 * 1024 * 8;
		}

		return Double.parseDouble(text);
	}

	public interface MonitorRule {
		public Pair<Boolean, String> checkData(double[] values, double[] baselineValues);
	}
}
