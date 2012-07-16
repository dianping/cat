package com.dianping.cat.report.page.task;

import java.util.Arrays;
import java.util.List;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	
	private long date;
	
	private List<String> domains;
	
	private List<String> names;
	
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
	
	//TODO get the domains
	public List<String> getDomains(){
		return Arrays.asList("All","Cat","Mobile","TuanGou");
	}
	
	//TODO get the names
	public List<String> getNames(){
		return Arrays.asList("All","event","problem","heartbeat","transaction");
	}
	
	//TODO get the current Domain
	public String getDomain(){
		return "All";
	}
	
	//TODO get the current name
	public String getName(){
		return "All";
	}
	public long getDate() {
   	return date;
   }

	public void setDate(long date) {
   	this.date = date;
   }
/*
	public void setNames(List<String> names) {
   	this.names = names;
   }

	public void setDomains(List<String> domains) {
   	this.domains = domains;
   }*/
}
