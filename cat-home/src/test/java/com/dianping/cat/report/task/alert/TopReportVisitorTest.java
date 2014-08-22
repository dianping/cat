package com.dianping.cat.report.task.alert;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.home.alert.report.entity.AlertReport;
import com.dianping.cat.home.dependency.exception.entity.ExceptionExclude;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.report.task.alert.exception.TopReportVisitor;
import com.dianping.cat.system.config.ExceptionConfigManager;

public class TopReportVisitorTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		String topReportXml = Files.forIO().readFrom(getClass().getResourceAsStream("topReport.xml"), "utf-8");
		TopReport topReport = com.dianping.cat.consumer.top.model.transform.DefaultSaxParser.parse(topReportXml);

		String expectedAlertReportXml = Files.forIO()
		      .readFrom(getClass().getResourceAsStream("alertReport.xml"), "utf-8");

		ExceptionConfigMock exceptionConfigManager = new ExceptionConfigMock();
		AlertReport alertReport = new AlertReport(Constants.CAT);
		TopReportVisitor visitor = new TopReportVisitor().setReport(alertReport).setExceptionConfigManager(
				exceptionConfigManager);

		alertReport.setStartTime(topReport.getStartTime());
		alertReport.setEndTime(topReport.getEndTime());
		visitor.visitTopReport(topReport);

		Assert.assertEquals("Check the report result!", expectedAlertReportXml.replace("\r", ""), alertReport.toString()
		      .replace("\r", ""));
	}

	public class ExceptionConfigMock extends ExceptionConfigManager {

		private Map<String, ExceptionLimit> exceptionLimitMap = new HashMap<String, ExceptionLimit>();

		private Map<String, ExceptionExclude> exceptionExcludeMap = new HashMap<String, ExceptionExclude>();

		public ExceptionConfigMock() {
			// exception limit
			ExceptionLimit exceptionAA = new ExceptionLimit("exceptionA");
			exceptionAA.setDomain("domainA").setError(10).setWarning(5);
			exceptionLimitMap.put(exceptionAA.getDomain() + "_" + exceptionAA.getId(), exceptionAA);

			ExceptionLimit exceptionAT = new ExceptionLimit("Total");
			exceptionAT.setDomain("domainA").setError(20).setWarning(10);
			exceptionLimitMap.put(exceptionAT.getDomain(), exceptionAT);

			ExceptionLimit exceptionBA = new ExceptionLimit("exceptionA");
			exceptionBA.setDomain("domainB").setError(10).setWarning(5);
			exceptionLimitMap.put(exceptionBA.getDomain() + "_" + exceptionBA.getId(), exceptionBA);

			ExceptionLimit exceptionCA = new ExceptionLimit("exceptionA");
			exceptionCA.setDomain("domainC").setError(10).setWarning(5);
			exceptionLimitMap.put(exceptionCA.getDomain() + "_" + exceptionCA.getId(), exceptionCA);

			ExceptionLimit exceptionCB = new ExceptionLimit("exceptionB");
			exceptionCB.setDomain("domainC").setError(10).setWarning(5);
			exceptionLimitMap.put(exceptionCB.getDomain() + "_" + exceptionCB.getId(), exceptionCB);

			ExceptionLimit exceptionCC = new ExceptionLimit("exceptionC");
			exceptionCC.setDomain("domainC").setError(10).setWarning(5);
			exceptionLimitMap.put(exceptionCC.getDomain() + "_" + exceptionCC.getId(), exceptionCC);

			ExceptionLimit exceptionCD = new ExceptionLimit("exceptionD");
			exceptionCD.setDomain("domainC").setError(10).setWarning(5);
			exceptionLimitMap.put(exceptionCD.getDomain() + "_" + exceptionCD.getId(), exceptionCD);

			ExceptionLimit exceptionCT = new ExceptionLimit("Total");
			exceptionCT.setDomain("domainC").setError(20).setWarning(10);
			exceptionLimitMap.put(exceptionCT.getDomain(), exceptionCT);

			// exception exclude
			ExceptionExclude exceptionAll = new ExceptionExclude("All");
			exceptionAll.setDomain("domainA");
			exceptionExcludeMap.put(exceptionAll.getDomain() + "_" + exceptionAll.getId(), exceptionAll);

			ExceptionExclude exceptionCAExclude = new ExceptionExclude("exceptionA");
			exceptionCAExclude.setDomain("domainC");
			exceptionExcludeMap.put(exceptionCAExclude.getDomain() + "_" + exceptionCAExclude.getId(), exceptionCAExclude);

		}

		public ExceptionLimit queryDomainExceptionLimit(String domain, String exceptionName) {
			return exceptionLimitMap.get(domain + "_" + exceptionName);
		}

		public ExceptionLimit queryDomainTotalLimit(String domain) {
			return exceptionLimitMap.get(domain);
		}

		public ExceptionExclude queryDomainExceptionExclude(String domain, String exceptionName) {
			ExceptionExclude exceptionExclude = null;
			exceptionExclude = exceptionExcludeMap.get(domain + "_" + exceptionName);
			if (exceptionExclude == null) {
				exceptionExclude = exceptionExcludeMap.get(domain + "_All");
			}
			return exceptionExclude;
		}
	}
}
