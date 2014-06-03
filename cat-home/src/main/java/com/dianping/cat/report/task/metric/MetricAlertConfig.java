package com.dianping.cat.report.task.metric;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.tuple.Pair;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.core.dal.Project;
import com.site.helper.Splitters;

public class MetricAlertConfig extends BaseAlertConfig {

	public List<String> buildSMSReceivers(ProductLine productLine) {
		List<String> phones = new ArrayList<String>();
		String phonesList = productLine.getPhone();

		phones.add("13916536843");// 值班
		phones.add("18616671676");// 尤勇
		phones.add("13858086694");// 黄河

		if (phonesList != null) {
			phones.addAll(Splitters.by(",").noEmptyItem().split(phonesList));
		}
		return phones;
	}

	public List<String> buildMailReceivers(ProductLine productLine) {
		List<String> emails = new ArrayList<String>();
		String emailList = productLine.getEmail();

		emails.add("yong.you@dianping.com");
		emails.add("jialin.sun@dianping.com");
		emails.add("argus@dianping.com");
		emails.add("monitor@dianping.com");

		if (emailList != null) {
			emails.addAll(Splitters.by(",").noEmptyItem().split(emailList));
		}
		return emails;
	}

	public List<String> buildMailReceivers(Project project) {
		List<String> emails = new ArrayList<String>();
		String emailList = project.getEmail();

		emails.add("jialin.sun@dianping.com");
		if (emailList != null) {
			emails.addAll(Splitters.by(",").noEmptyItem().split(emailList));
		}
		return emails;
	}

	public Pair<Boolean, String> checkData(MetricItemConfig config, double[] value, double[] baseline, MetricType type) {
		int length = value.length;
		StringBuilder baselines = new StringBuilder();
		StringBuilder values = new StringBuilder();
		double decreasePercent = config.getDecreasePercentage();
		double decreaseValue = config.getDecreaseValue();
		double valueSum = 0;
		double baselineSum = 0;
		DecimalFormat df = new DecimalFormat("0.0");

		if (decreasePercent == 0) {
			decreasePercent = 50;
		}
		if (decreaseValue == 0) {
			decreaseValue = 100;
		}

		for (int i = 0; i < length; i++) {
			baselines.append(df.format(baseline[i])).append(" ");
			values.append(df.format(value[i])).append(" ");
			valueSum = valueSum + value[i];
			baselineSum = baselineSum + baseline[i];

			if (baseline[i] <= 0) {
				baseline[i] = 100;
				return new Pair<Boolean, String>(false, "");
			}
			if (type == MetricType.COUNT || type == MetricType.SUM) {
				if (value[i] / baseline[i] > (1 - decreasePercent / 100) || (baseline[i] - value[i]) < decreaseValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}
		}
		double percent = (1 - valueSum / baselineSum) * 100;
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		sb.append("[基线值:").append(baselines.toString()).append("] ");
		sb.append("[实际值:").append(values.toString()).append("] ");
		sb.append("[下降:").append(df.format(percent)).append("%").append("]");
		sb.append("[告警时间:").append(sdf.format(new Date()) + "]");
		return new Pair<Boolean, String>(true, sb.toString());
	}

}
