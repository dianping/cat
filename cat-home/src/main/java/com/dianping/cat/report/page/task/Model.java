package com.dianping.cat.report.page.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.web.mvc.ViewModel;

import com.dainping.cat.consumer.dal.report.Task;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.view.StringSortHelper;
import com.dianping.cat.report.view.TaskUrlNav;

public class Model extends ViewModel<ReportPage, Action, Context> {

	private long date;

	private String domain;

	private List<String> domains;

	private Date from;

	private String name;

	private List<String> names;

	private int numOfFailureTasks;

	private int pageSize;

	private boolean redoResult;

	private int status;

	private List<Task> tasks;

	private Date to;

	private int totalNumOfTasks;

	private int totalpages;

	private int type;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getBaseUri() {
		return buildPageUri(getPage().getPath(), null);
	}

	public long getDate() {
		return date;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getDomain() {
		return domain;
	}

	public List<String> getDomains() {
		if (domains == null) {
			domains = new ArrayList<String>();
		} else {
			StringSortHelper.sortDomain(domains);
		}
		return domains;
	}

	public Date getFrom() {
		return from;
	}

	public String getName() {
		return name;
	}

	public List<String> getNames() {
		return names;
	}

	public TaskUrlNav[] getNavs() {
		return TaskUrlNav.values();
	}

	public int getNumOfFailureTasks() {
		return numOfFailureTasks;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getStatus() {
		return status;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public Date getTo() {
		return to;
	}

	public int getTotalNumOfTasks() {
		return totalNumOfTasks;
	}

	public int getTotalpages() {
		return totalpages;
	}

	public int getType() {
		return type;
	}

	public boolean isRedoResult() {
		return redoResult;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}

	public void setNumOfFailureTasks(int numOfFailureTasks) {
		this.numOfFailureTasks = numOfFailureTasks;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setRedoResult(boolean redoResult) {
		this.redoResult = redoResult;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public void setTotalNumOfTasks(int totalNumOfTasks) {
		this.totalNumOfTasks = totalNumOfTasks;
	}

	public void setTotalpages(int totalpages) {
		this.totalpages = totalpages;
	}

	public void setType(int type) {
		this.type = type;
	}
}
