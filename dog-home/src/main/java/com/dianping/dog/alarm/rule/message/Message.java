package com.dianping.dog.alarm.rule.message;

import java.util.List;

public class Message {
	
	private String content;
	
	private String title;
	
	private List<String> receiver;

	public String getContent() {
   	return content;
   }

	public void setContent(String content) {
   	this.content = content;
   }

	public String getTitle() {
   	return title;
   }

	public void setTitle(String title) {
   	this.title = title;
   }

	public List<String> getReceiver() {
   	return receiver;
   }

	public void setReceiver(List<String> receiver) {
   	this.receiver = receiver;
   }
	
	
}
