package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.alertconfig.entity.Receiver;
import com.dianping.cat.system.config.AlertConfigManager;
import com.site.helper.Splitters;
import com.site.lookup.util.StringUtils;

public abstract class BaseAlertConfig {

	@Inject
	protected AlertConfigManager m_manager;

	public abstract String getID();

	public List<String> buildSMSReceivers(ProductLine productLine) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_manager.getReceiverById(getID());
		
		if(receiver!=null && !receiver.isEnable()){
			return smsReceivers;
		}

		smsReceivers.addAll(buildDefaultSMSReceivers(receiver));
		smsReceivers.addAll(buildProductlineSMSReceivers(productLine));

		return smsReceivers;
	}

	public List<String> buildMailReceivers(ProductLine productLine) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_manager.getReceiverById(getID());
		
		if(receiver!=null && !receiver.isEnable()){
			return mailReceivers;
		}

		mailReceivers.addAll(buildDefaultMailReceivers(receiver));
		mailReceivers.addAll(buildProductlineMailReceivers(productLine));

		return mailReceivers;
	}

	public List<String> buildMailReceivers(Project project) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_manager.getReceiverById(getID());
		
		if(receiver!=null && !receiver.isEnable()){
			return mailReceivers;
		}
		
		mailReceivers.addAll(buildDefaultMailReceivers(receiver));
		mailReceivers.addAll(buildProjectMailReceivers(project));
		
		return mailReceivers;
	}

	protected List<String> buildDefaultSMSReceivers(Receiver receiver) {
		List<String> smsReceivers = new ArrayList<String>();

		if (receiver == null) {
			return smsReceivers;
		} else {
			for (String phone : receiver.getPhones()) {
				if (!StringUtils.isEmpty(phone)) {
					smsReceivers.add(phone);
				}
			}
			return smsReceivers;
		}
	}

	protected List<String> buildDefaultMailReceivers(Receiver receiver) {
		List<String> mailReceivers = new ArrayList<String>();

		if (receiver == null) {
			return mailReceivers;
		} else {
			for (String email : receiver.getEmails()) {
				if (!StringUtils.isEmpty(email)) {
					mailReceivers.add(email);
				}
			}
		}

		return mailReceivers;
	}
	
	public List<String> buildProductlineSMSReceivers(ProductLine productLine) {
		List<String> phones = new ArrayList<String>();
		String phonesList = productLine.getPhone();

		if (phonesList != null) {
			phones.addAll(Splitters.by(",").noEmptyItem().split(phonesList));
		}
		
		return phones;
	}

	public List<String> buildProductlineMailReceivers(ProductLine productLine) {
		List<String> emails = new ArrayList<String>();
		String emailList = productLine.getEmail();

		if (emailList != null) {
			emails.addAll(Splitters.by(",").noEmptyItem().split(emailList));
		}
		
		return emails;
	}
	
	public List<String> buildProjectMailReceivers(Project project) {
		List<String> emails = new ArrayList<String>();
		String emailList = project.getEmail();
		
		if (emailList != null) {
			emails.addAll(Splitters.by(",").noEmptyItem().split(emailList));
		}
		return emails;
	}

	public String buildMailTitle(ProductLine productLine, MetricItemConfig config) {
		StringBuilder sb = new StringBuilder();

		sb.append("[业务告警] [产品线 ").append(productLine.getTitle()).append("]");
		sb.append("[业务指标 ").append(config.getTitle()).append("]");
		return sb.toString();
	}
}
