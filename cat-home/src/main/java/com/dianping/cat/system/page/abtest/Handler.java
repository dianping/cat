package com.dianping.cat.system.page.abtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestEntity;
import com.dianping.cat.system.SystemPage;

public class Handler implements PageHandler<Context> {

	private final int m_pageSize = 5;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AbtestDao m_abtestDao;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "abtest")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// TODO
	}

	@Override
	@OutboundActionMeta(name = "abtest")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		if (action == Action.LIST) {
			renderListView(model, payload);
		} else if (action == Action.VIEW) {

		} else if (action == Action.REPORT) {

		}

		model.setAction(action);
		model.setPage(SystemPage.ABTEST);
		m_jspViewer.view(ctx, model);
	}

	private void renderListView(Model model, Payload payload) {
		List<ABTestReport> reports = new ArrayList<ABTestReport>();
		List<Abtest> entities = new ArrayList<Abtest>();
		AbtestStatus status = AbtestStatus.getByName(payload.getStatus(), null);
		Date now = new Date();

		try {
			entities = m_abtestDao.findAllAbtest(AbtestEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}

		List<Abtest> filterTests = new ArrayList<Abtest>();
		int createdCount = 0, readyCount = 0, runningCount = 0, terminatedCount = 0, suspendedCount = 0;

		for (Abtest abtest : entities) {
			ABTestReport report = new ABTestReport(abtest, now);

			if (status != null && report.getStatus() == status) {
				filterTests.add(abtest);
			}
			switch (report.getStatus()) {
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

		model.setCreatedCount(createdCount);
		model.setReadyCount(readyCount);
		model.setRunningCount(runningCount);
		model.setTerminatedCount(terminatedCount);
		model.setSuspendedCount(suspendedCount);
		if (status != null) {
			entities = filterTests;
		}

		int totalSize = entities.size();
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
		for(int i = fromIndex ; i < toIndex ; i++){
			reports.add(new ABTestReport(entities.get(i), now));
		}
			
		model.setTotalPages(totalPages);
		model.setDate(now);
		model.setReports(reports);
	}
}
