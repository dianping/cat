package com.dianping.cat.report.task.exceptionAlert;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.home.alertReport.entity.AlertReport;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.system.config.ExceptionThresholdConfigManager;

public class TopReportVisitorTest extends ComponentTestCase {

	@Test
	public void testMerge() throws Exception {

		String topReportXml = Files.forIO().readFrom(getClass().getResourceAsStream("topReport.xml"), "utf-8");
		TopReport topReport = com.dianping.cat.consumer.top.model.transform.DefaultSaxParser.parse(topReportXml);

		String expectedAlertReportXml = Files.forIO()
		      .readFrom(getClass().getResourceAsStream("alertReport.xml"), "utf-8");

		ConfigMock m_configManager = new ConfigMock();
		AlertReport alertReport = new AlertReport(Constants.CAT);
		TopReportVisitor visitor = new TopReportVisitor(m_configManager).setReport(alertReport);

		alertReport.setStartTime(topReport.getStartTime());
		alertReport.setEndTime(topReport.getEndTime());
		visitor.visitTopReport(topReport);

		Assert.assertEquals("Check the merge result!", expectedAlertReportXml.replace("\r", ""), alertReport.toString()
		      .replace("\r", ""));
	}

	public class ConfigMock extends ExceptionThresholdConfigManager {

		private Map<String, ExceptionLimit> exceptionMap = new HashMap<String, ExceptionLimit>();

		public ConfigMock() {
			ExceptionLimit exceptionAA = new ExceptionLimit("exceptionA");
			exceptionAA.setDomain("domainA").setError(10).setWarning(5);
			exceptionMap.put(exceptionAA.getDomain() + "_" + exceptionAA.getId(), exceptionAA);

			ExceptionLimit exceptionAT = new ExceptionLimit("Total");
			exceptionAT.setDomain("domainA").setError(20).setWarning(10);
			exceptionMap.put(exceptionAT.getDomain(), exceptionAT);

			ExceptionLimit exceptionBA = new ExceptionLimit("exceptionA");
			exceptionBA.setDomain("domainB").setError(10).setWarning(5);
			exceptionMap.put(exceptionBA.getDomain() + "_" + exceptionBA.getId(), exceptionBA);

			ExceptionLimit exceptionCA = new ExceptionLimit("exceptionA");
			exceptionCA.setDomain("domainC").setError(10).setWarning(5);
			exceptionMap.put(exceptionCA.getDomain() + "_" + exceptionCA.getId(), exceptionCA);

			ExceptionLimit exceptionCB = new ExceptionLimit("exceptionB");
			exceptionCB.setDomain("domainC").setError(10).setWarning(5);
			exceptionMap.put(exceptionCB.getDomain() + "_" + exceptionCB.getId(), exceptionCB);

			ExceptionLimit exceptionCC = new ExceptionLimit("exceptionC");
			exceptionCC.setDomain("domainC").setError(10).setWarning(5);
			exceptionMap.put(exceptionCC.getDomain() + "_" + exceptionCC.getId(), exceptionCC);

			ExceptionLimit exceptionCD = new ExceptionLimit("exceptionD");
			exceptionCD.setDomain("domainC").setError(10).setWarning(5);
			exceptionMap.put(exceptionCD.getDomain() + "_" + exceptionCD.getId(), exceptionCD);

			ExceptionLimit exceptionCT = new ExceptionLimit("Total");
			exceptionCT.setDomain("domainC").setError(20).setWarning(10);
			exceptionMap.put(exceptionCT.getDomain(), exceptionCT);

		}

		public ExceptionLimit queryDomainExceptionLimit(String domain, String exceptionName) {
			return exceptionMap.get(domain + "_" + exceptionName);
		}

		public ExceptionLimit queryDomainTotalLimit(String domain) {
			return exceptionMap.get(domain);
		}
	}

}
