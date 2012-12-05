package com.dianping.cat.system.alarm.template;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.home.template.entity.ThresholdTemplate;
import com.dianping.cat.home.template.transform.DefaultDomParser;
import com.dianping.cat.system.alarm.threshold.template.ThresholdTemplateMerger;


public class TemplateMergerTest {
	@Test
	public void testEventReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("threshold-template-new.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("threshold-template-old.xml"), "utf-8");
		ThresholdTemplate templateOld = new DefaultDomParser().parse(oldXml);
		ThresholdTemplate templateNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("threshold-template-mergeResult.xml"), "utf-8");
		ThresholdTemplateMerger merger = new ThresholdTemplateMerger(new ThresholdTemplate());

		templateNew.accept(merger);
		templateOld.accept(merger);
		
		ThresholdTemplate mergeResult = merger.getThresholdTemplate();
		//Assert.assertEquals("Check the merge result!", expected, mergeResult.toString());
		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), mergeResult.toString().replaceAll("\\s*", ""));
	}
}
