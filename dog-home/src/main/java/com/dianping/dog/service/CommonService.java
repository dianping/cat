package com.dianping.dog.service;

import java.util.List;

public interface CommonService {
	public boolean sendEmail(String body, String title, String email);

	public boolean sendSmsMessage(String content, String phoneNumber);

	public boolean sendSmsMessage(String content, List<String> numbers);

	public boolean sendEmail(String body, String title, List<String> emails);
}
