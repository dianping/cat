package com.dianping.cat.report.page.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.hadoop.dal.TaskEntity;
import com.dianping.cat.report.ReportPage;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	private static final int PAGE_SIZE = 1000000;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private TaskDao taskDao;

	private static final String ALL = "All";

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "task")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "task")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		switch (payload.getAction()) {
		case VIEW:
			normalizeAndGetTaskData(payload, model);
			break;
		}
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.TASK);

		m_jspViewer.view(ctx, model);
	}
	
	public void normalizeAndGetTaskData(Payload payload, Model model) {
		String domain = payload.getDomain();
		String name = payload.getName();
		String queryDomain = domain;
		String queryName = name;
		model.setType(payload.getType());
		model.setStatus(payload.getStatus());

		if (isEmpty(domain) || ALL.equals(domain)) {
			model.setDomain(ALL);
			queryDomain = null;
		} else {
			model.setDomain(domain);
		}

		if (isEmpty(name) || ALL.equals(name)) {
			model.setName(ALL);
			queryName = null;
		} else {
			model.setName(name);
		}

		Date start = new Date(payload.getStartDate());
		Date end = new Date(payload.getEndDate());
		model.setDate(start.getTime());
		model.setFrom(start);
		model.setTo(end);
		
		getTaskData(payload,model,start,end,queryName,queryDomain);
		
	}

	private void getTaskData(Payload payload, Model model,Date start,Date end,String queryName,String queryDomain) {
		int totalPages = 0;
		int totalNumOfTask = 0;
		int type = payload.getType();
		int status = payload.getStatus();

		List<String> domains = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		names.add(ALL);
		domains.add(ALL);
		List<Task> tasks = new ArrayList<Task>();
		try {
			List<Task> domainSet = taskDao.findAllDistinct(start, end, TaskEntity.READSET_REPORT_DOMAIN);
			List<Task> nameSet = taskDao.findAllDistinct(start, end, TaskEntity.READSET_REPORT_NAME);
			for (Task task : domainSet) {
				domains.add(task.getReportDomain());
			}
			for (Task task : nameSet) {
				names.add(task.getReportName());
			}
			model.setDomains(domains);
			model.setNames(names);

			List<Task> totalTasks = taskDao.findAll(status, start, end, queryName, queryDomain, type,
			      TaskEntity.READSET_COUNT);
			totalNumOfTask = totalTasks.get(0).getCount();
			totalPages = (int) Math.floor((double) totalNumOfTask / (double) PAGE_SIZE);
			model.setTotalpages(totalPages);

			// get the page dates
			int currentPage = payload.getCurrentPage() == 0 ? 1 : payload.getCurrentPage();
			int startLimit = (currentPage - 1) * PAGE_SIZE;


			tasks = this.taskDao.findByStatusTypeName(status, start, end, queryName, queryDomain, type, startLimit,
			      PAGE_SIZE, TaskEntity.READSET_FULL);
			model.setTasks(tasks);
		} catch (DalException e) {
			Cat.getProducer().logError(e);
		}
   }


	
	private boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
}
