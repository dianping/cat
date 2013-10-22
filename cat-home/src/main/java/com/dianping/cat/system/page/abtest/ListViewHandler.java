package com.dianping.cat.system.page.abtest;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestEntity;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.AbtestRunDao;
import com.dianping.cat.home.dal.abtest.AbtestRunEntity;
import com.dianping.cat.system.page.abtest.ListViewModel.AbtestItem;
import com.dianping.cat.system.page.abtest.util.AbtestStatus;

public class ListViewHandler implements SubHandler {
	@Inject
	private AbtestDao m_abtestDao;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	@Inject
	private int m_pageSize = 10;

	@Override
	public void handle(Context ctx, Model model, Payload payload) {
		ListViewModel listViewModel = new ListViewModel();
		AbtestStatus status = AbtestStatus.getByName(payload.getStatus(), null);

		List<AbtestItem> filterItems = new ArrayList<AbtestItem>();
		List<AbtestItem> totalItems = new ArrayList<AbtestItem>();
		int createdCount = 0, readyCount = 0, runningCount = 0, terminatedCount = 0, suspendedCount = 0;

		List<AbtestRun> runs = new ArrayList<AbtestRun>();

		try {
			runs = m_abtestRunDao.findAll(AbtestRunEntity.READSET_FULL);

			for (AbtestRun run : runs) {
				Abtest abtest = m_abtestDao.findByPK(run.getCaseId(), AbtestEntity.READSET_FULL);
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

	public void setAbtestDao(AbtestDao abtestDao) {
   	m_abtestDao = abtestDao;
   }

	public void setAbtestRunDao(AbtestRunDao abtestRunDao) {
   	m_abtestRunDao = abtestRunDao;
   }

	public void setPageSize(int pageSize) {
   	m_pageSize = pageSize;
   }
}
