package com.dianping.cat.system.tool;

import java.util.List;

public interface MailSMS {

	public boolean sendEmail(String title, String content, List<String> emails);
	
	public boolean sendEmailByGmail(String title,String content,List<String> emails);
	
	public boolean sendSms(String title,String content,List<String> phones);

}
