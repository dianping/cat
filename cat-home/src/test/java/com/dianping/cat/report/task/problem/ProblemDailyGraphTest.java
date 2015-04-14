package com.dianping.cat.report.task.problem;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.problem.task.ProblemDailyGraphCreator;

public class ProblemDailyGraphTest {

	@Test
	public void test() throws Exception {
		String expeted = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemMergerDaily.xml"), "utf-8");
		ProblemReport report = DefaultSaxParser.parse(expeted);
		ProblemDailyGraphCreator creator = new ProblemDailyGraphCreator();
		
		creator.visitProblemReport(report);

		Assert.assertEquals(2, creator.buildDailyGraph().size());
	}
}
