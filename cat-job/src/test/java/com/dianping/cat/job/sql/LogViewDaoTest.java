package com.dianping.cat.job.sql;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.sql.dal.Logview;
import com.dianping.cat.job.sql.dal.LogviewDao;
import com.dianping.cat.job.sql.dal.LogviewEntity;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class LogViewDaoTest extends ComponentTestCase {
	@Test
	public void testDao() throws Exception {
		LogviewDao dao = lookup(LogviewDao.class);
		Logview logview = dao.findNextByMessageIdTags("id1", true, "pet", null, null, LogviewEntity.READSET_FULL);

		System.out.println(logview);

		logview = dao.findNextByMessageIdTags("id2", false, "pet", null, null, LogviewEntity.READSET_FULL);

		System.out.println(logview);
	}

}
