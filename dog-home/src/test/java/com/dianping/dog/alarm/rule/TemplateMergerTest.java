package com.dianping.dog.alarm.rule;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.dog.home.template.model.entity.ThresholdTemplate;
import com.dianping.cat.dog.home.template.model.transform.DefaultDomParser;


public class TemplateMergerTest {
	@Test
	public void testEventReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("threshold-template-new.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("threshold-template-old.xml"), "utf-8");
		ThresholdTemplate templateOld = new DefaultDomParser().parse(oldXml);
		ThresholdTemplate templateNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("threshold-template-mergeResult.xml"), "utf-8");
		TemplateMerger merger = new TemplateMerger(new ThresholdTemplate());

		templateOld.accept(merger);
		templateNew.accept(merger);
		ThresholdTemplate mergeResult = merger.getThresholdTemplate();
		String resultStr = mergeResult.toString().replaceAll("\\s*", "");
		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), resultStr);
	}
}
