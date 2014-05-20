package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.List;

import org.unidal.tuple.Pair;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.monitorrules.entity.Condition;
import com.dianping.cat.home.monitorrules.entity.Config;

public class SwitchAlertConfig extends BaseAlertConfig {

	public List<String> buildExceptionSMSReceivers(ProductLine productLine) {
		List<String> phones = new ArrayList<String>();

		//phones.add("18662513308");
		return phones;
	}

	public List<String> buildMailReceivers(ProductLine productLine) {
		List<String> emails = new ArrayList<String>();
		// String emailList = productLine.getEmail();

		emails.add("leon.li@dianping.com");
		// emails.addAll(Splitters.by(",").noEmptyItem().split(emailList));
		return emails;
	}

	public List<String> buildMailReceivers(Project project) {
		List<String> emails = new ArrayList<String>();
		// String emailList = project.getEmail();

		emails.add("leon.li@dianping.com");
		// emails.addAll(Splitters.by(",").noEmptyItem().split(emailList));
		return emails;
	}

	public List<String> buildSMSReceivers(ProductLine productLine) {
		List<String> phones = new ArrayList<String>();
		// String phonesList = productLine.getPhone();

		//phones.add("18662513308");
		// phones.addAll(Splitters.by(",").noEmptyItem().split(phonesList));
		return phones;
	}

	public Pair<Boolean, String> checkData(double[] value, double[] baseline, MetricType type, List<Config> configs) {
		for (Config con : configs) {
			int dataLength = queryMaxMinute(con);

			double[] validVal = buildLastMinutes(value, dataLength);
			double[] validBase = buildLastMinutes(baseline, dataLength);
			Pair<Boolean, String> result = checkDataByConfig(validVal, validBase, type, con);

			if (result.getKey() == true) {
				return result;
			}
		}
		return new Pair<Boolean, String>(false, "");
	}


	private int queryMaxMinute(Config con) {
		int maxMinute = 0;
		for (Condition condition : con.getConditions()) {
			int tmpMinute = condition.getMinute();
			if (tmpMinute > maxMinute) {
				maxMinute = tmpMinute;
			}
		}
		return maxMinute;
	}

}
