package com.dianping.cat.system.tool;

import java.util.List;

public interface MailSMS {

	public boolean sendEmail(String title,String content,List<String>emails);
	
	public boolean sendSMS(String content,List<String> phones);
}
