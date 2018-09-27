package com.dianping.cat.report.task.project;

import java.io.File;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.service.ProjectService;

public class ProjectBuilderTest extends ComponentTestCase {

	@Before
	public void before() throws Exception {
		ServerConfigManager manager = lookup(ServerConfigManager.class);

		manager.initialize(new File("/data/appdatas/cat/server.xml"));
	}

	@Test
	public void test() throws ParseException {
		lookup(ProjectService.class);
		
		ProjectUpdateTask builder = (ProjectUpdateTask) lookup(ProjectUpdateTask.class);

		builder.deleteUnusedDomainInfo();

	}

}
