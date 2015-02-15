package com.dianping.cat.report.page.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.cache.CacheReport.CacheNameItem;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class CacheOutputTest extends ComponentTestCase {

	private void addCacheReportToResult(String domain, CacheReport cacheReport, Map<String, List<String>> result) {
		List<String> ids = new ArrayList<String>();

		for (CacheNameItem nameItem : cacheReport.getNameItems()) {
			String currentId = nameItem.getName().getId();

			if (!"ALL".equals(currentId)) {
				ids.add(currentId);
			}
		}
		result.put(domain, ids);
	}

	private CacheReport generateCacheReport(ReportServiceManager manager, String domain, Date startDate, Date endDate) {
		TransactionReport transactionReport = manager.queryTransactionReport(domain, startDate, endDate);
		EventReport eventReport = manager.queryEventReport(domain, startDate, endDate);
		String type = "Cache.web";
		String queryName = "";
		String ip = "All";
		TransactionReportVistor visitor = new TransactionReportVistor();

		visitor.setType(type).setQueryName(queryName).setCurrentIp(ip);
		visitor.setEventReport(eventReport);
		visitor.visitTransactionReport(transactionReport);
		return visitor.getCacheReport();
	}

	@Test
	public void printCacheWebInfos() {
		ReportServiceManager manager = lookup(ReportServiceManager.class);
		Date endDate = TimeHelper.getCurrentDay();
		Date startDate = TimeHelper.addDays(endDate, -7);
		Set<String> domains = queryDomains();
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		for (String domain : domains) {
			CacheReport cacheReport = generateCacheReport(manager, domain, startDate, endDate);

			if (cacheReport != null) {
				addCacheReportToResult(domain, cacheReport, result);
			}
		}
		System.out.println(new JsonBuilder().toJson(result));
	}

	private Set<String> queryDomains() {
		@SuppressWarnings("unchecked")
		ModelService<TransactionReport> transactionService = lookup(ModelService.class, TransactionAnalyzer.ID);
		ModelRequest request = new ModelRequest("cat", System.currentTimeMillis());
		Set<String> domains = new HashSet<String>();

		if (transactionService.isEligable(request)) {
			ModelResponse<TransactionReport> response = transactionService.invoke(request);
			domains.addAll(response.getModel().getDomainNames());
		}
		return domains;
	}
}
