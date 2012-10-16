package com.dianping.cat.system.tool;

import java.util.List;


public class MailSMSImpl implements MailSMS {

	@Override
	public boolean sendEmail(String title, String content, List<String> emails) {
		return false;
	}

	@Override
	public boolean sendSMS(String content, List<String> phones) {
		return false;
	}

}
