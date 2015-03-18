package com.dianping.cat.report.task.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class CommandAutoCompleterTest extends ComponentTestCase {

	@Test
	public void testDailyTask() {
		CommandAutoCompleter builder = lookup(CommandAutoCompleter.class);

		try {
			builder.autoCompleteDomain(new SimpleDateFormat("yyyy-MM-dd").parse("2015-03-16"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
