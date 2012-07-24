package com.dianping.cat.report.page.task;

import java.util.Date;
import java.util.List;

import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.view.TaskUrlNav;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	
	private long date;
	
	private List<String> domains;
	
	private List<String> names;
	
	private int totalpages;
	
	private int pageSize;
	
	private List<Task> tasks;
	
	private Date from;
	
	private Date to;
	
	private String domain;
	
	private String name;
	
	private int type;
	
	private int status;
	
	private int totalNumOfTasks;
	
	private int numOfFailureTasks;
	
	
	public int getNumOfFailureTasks() {
   	return numOfFailureTasks;
   }

	public void setNumOfFailureTasks(int numOfFailureTasks) {
   	this.numOfFailureTasks = numOfFailureTasks;
   }

	public int getTotalNumOfTasks() {
   	return totalNumOfTasks;
   }

	public void setTotalNumOfTasks(int totalNumOfTasks) {
   	this.totalNumOfTasks = totalNumOfTasks;
   }

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
	
	public String getBaseUri() {
		return buildPageUri(getPage().getPath(), null);
	}
	
	public String getDomain(){
		return domain;
	}
	
	public String getName(){
		return name;
	}
	public long getDate() {
   	return date;
   }

	public void setDate(long date) {
   	this.date = date;
   }

	public int getTotalpages() {
   	return totalpages;
   }

	public void setTotalpages(int totalpages) {
   	this.totalpages = totalpages;
   }

	public List<String> getDomains() {
   	return domains;
   }

	public void setDomains(List<String> domains) {
   	this.domains = domains;
   }

	public List<String> getNames() {
   	return names;
   }

	public void setNames(List<String> names) {
   	this.names = names;
   }

	public int getPageSize() {
   	return pageSize;
   }

	public void setPageSize(int pageSize) {
   	this.pageSize = pageSize;
   }

	public List<Task> getTasks() {
   	return tasks;
   }

	public void setTasks(List<Task> tasks) {
   	this.tasks = tasks;
   }
	
	public TaskUrlNav[] getNavs() {
		return TaskUrlNav.values();
	}

	public Date getFrom() {
   	return from;
   }

	public void setFrom(Date from) {
   	this.from = from;
   }

	public Date getTo() {
   	return to;
   }

	public void setTo(Date to) {
   	this.to = to;
   }

	public void setDomain(String domain) {
   	this.domain = domain;
   }

	public void setName(String name) {
   	this.name = name;
   }

	public int getType() {
   	return type;
   }

	public void setType(int type) {
   	this.type = type;
   }

	public int getStatus() {
   	return status;
   }

	public void setStatus(int status) {
   	this.status = status;
   }
}
