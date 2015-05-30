package com.dianping.cat.problem;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.problem.model.entity.ProblemReport;
import com.dianping.cat.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.problem.task.ProblemDailyGraphCreator;

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
