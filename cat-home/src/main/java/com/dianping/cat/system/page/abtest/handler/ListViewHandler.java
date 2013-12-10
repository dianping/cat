package com.dianping.cat.system.page.abtest.handler;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.ErrorObject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.system.page.abtest.Context;
import com.dianping.cat.system.page.abtest.Model;
import com.dianping.cat.system.page.abtest.Payload;
import com.dianping.cat.system.page.abtest.handler.ListViewModel.AbtestItem;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.dianping.cat.system.page.abtest.util.AbtestStatus;

public class ListViewHandler implements SubHandler {
	
	public static final String ID = "listview_handler";
	
	@Inject
	private int m_pageSize = 10;

	@Inject
	private ABTestService m_service;

	@Override
	public void handleOutbound(Context ctx, Model model, Payload payload) {
		ListViewModel listViewModel = new ListViewModel();
		AbtestStatus status = AbtestStatus.getByName(payload.getStatus(), null);

		List<AbtestItem> filterItems = new ArrayList<AbtestItem>();
		List<AbtestItem> totalItems = new ArrayList<AbtestItem>();
		int createdCount = 0, readyCount = 0, runningCount = 0, terminatedCount = 0, suspendedCount = 0;

		try {
			List<AbtestRun> runs = m_service.getAllAbtestRun();

			if (runs != null) {
				for (AbtestRun run : runs) {
					Abtest abtest = m_service.getABTestByCaseId(run.getCaseId());
					AbtestItem item = new AbtestItem(abtest, run);

					totalItems.add(item);

					if (status != null && item.getStatus() == status) {
						filterItems.add(item);
					}

					switch (item.getStatus()) {
					case CREATED:
						createdCount++;
						break;
					case READY:
						readyCount++;
						break;
					case RUNNING:
						runningCount++;
						break;
					case TERMINATED:
						terminatedCount++;
						break;
					case SUSPENDED:
						suspendedCount++;
						break;
					}
				}
			}
		} catch (Throwable e) {
			Cat.logError(e);
		}

		listViewModel.setCreatedCount(createdCount);
		listViewModel.setReadyCount(readyCount);
		listViewModel.setRunningCount(runningCount);
		listViewModel.setTerminatedCount(terminatedCount);
		listViewModel.setSuspendedCount(suspendedCount);

		if (status != null) {
			totalItems = null;
			totalItems = filterItems;
		}

		int totalSize = totalItems.size();
		int totalPages = totalSize % m_pageSize == 0 ? (totalSize / m_pageSize) : (totalSize / m_pageSize + 1);

		// safe guarder for pageNum
		if (payload.getPageNum() >= totalPages) {
			if (totalPages == 0) {
				payload.setPageNum(1);
			} else {
				payload.setPageNum(totalPages);
			}
		} else if (payload.getPageNum() <= 0) {
			payload.setPageNum(1);
		}

		int fromIndex = (payload.getPageNum() - 1) * m_pageSize;
		int toIndex = (fromIndex + m_pageSize) <= totalSize ? (fromIndex + m_pageSize) : totalSize;

		listViewModel.setTotalPages(totalPages);
		listViewModel.setItems(totalItems.subList(fromIndex, toIndex));

		model.setListViewModel(listViewModel);
	}

	public void setPageSize(int pageSize) {
		m_pageSize = pageSize;
	}

	@Override
	public void handleInbound(Context ctx, Payload payload) {
		handleStatusChangeAction(ctx);
	}

	private void handleStatusChangeAction(Context ctx) {
		Payload payload = ctx.getPayload();
		ErrorObject error = new ErrorObject("disable");
		String[] ids = payload.getIds();

		if (ids != null && ids.length != 0) {
			for (String id : ids) {
				try {
					int runID = Integer.parseInt(id);
					AbtestRun run = m_service.getAbTestRunById(runID);
					if (payload.getDisableAbtest() == -1) {
						// suspend abtest
						if (!run.isDisabled()) {
							run.setDisabled(true);
							m_service.updateAbtestRun(run);
						} else {
							error.addArgument(id, String.format("Abtest %s has been already suspended!", id));
						}
					} else if (payload.getDisableAbtest() == 1) {
						// resume abtest
						if (run.isDisabled()) {
							run.setDisabled(false);
							m_service.updateAbtestRun(run);
						} else {
							error.addArgument(id, String.format("Abtest %s has been already active!", id));
						}
					}
				} catch (Throwable e) {
					// do nothing
					Cat.logError(e);
				}
			}

			if (error.getArguments().isEmpty()) {
				ErrorObject success = new ErrorObject("success");
				ctx.addError(success);
			} else {
				ctx.addError(error);
			}
		}
	}
}
