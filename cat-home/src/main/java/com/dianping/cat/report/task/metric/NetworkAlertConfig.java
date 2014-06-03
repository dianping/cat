package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.core.dal.Project;

public class NetworkAlertConfig extends BaseAlertConfig {

	public List<String> buildSMSReceivers(ProductLine productLine) {
		List<String> phones = new ArrayList<String>();
		// String phonesList = productLine.getPhone();

		phones.add("18662513308");
		// phones.addAll(Splitters.by(",").noEmptyItem().split(phonesList));
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

}
