package com.dianping.cat.report.task.alert.sender;

import java.util.List;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.task.alert.BaseAlertConfig;

public class Postman {

	protected String generateMailTitle() {

		return null;
	}

	protected String generateMailContent() {

		return null;
	}

	protected List<String> queryReceivers() {

		return null;
	}

	public boolean storeAndSendAlert(BaseAlertConfig alertConfig, ProductLine productline, String domain) {

		
		
		return false;
	}

}
