package com.dianping.cat.abtest.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Splitters;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.abtest.spi.ABTestEntity;

public class ABTestRepositoryTest extends ComponentTestCase {
	private void checkHandler(String expected, String... keys) throws Exception {
		DefaultABTestEntityRepository repository = (DefaultABTestEntityRepository) lookup(ABTestEntityRepository.class);
		ProtocolMessage message = new ProtocolMessage();

		repository.setDomain("domain2");
		message.setName("heartbeat");

		AbtestModel abtest = new AbtestModel();

		for (String key : keys) {
			List<String> parts = Splitters.by(':').trim().split(key);
			int index = 0;
			int id = Integer.parseInt(parts.get(index++));
			String name = parts.get(index++);
			List<String> domains = Splitters.by(',').noEmptyItem().trim().split(parts.get(index++));
			Case _case = new Case(id).setName(name);

			_case.getDomains().addAll(domains);
			Run run = new Run(id);
			run.getDomains().addAll(domains);
			_case.addRun(run);
			abtest.addCase(_case);
		}

		message.setContent(abtest.toString());

		repository.handle(message);
		StringBuilder sb = new StringBuilder(1024);
		List<ABTestEntity> values = new ArrayList<ABTestEntity>(repository.getEntities().values());
		boolean first = true;

		Collections.sort(values, new Comparator<ABTestEntity>() {
			@Override
			public int compare(ABTestEntity o1, ABTestEntity o2) {
				return o1.getName().compareTo(o1.getName());
			}
		});

		for (ABTestEntity entity : values) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}

			sb.append(entity.getName());
		}

		Assert.assertEquals(expected, sb.toString());
	}

	@Test
	public void testHandler() throws Exception {
		checkHandler("case2,case3", "1:case1:domain1", "2:case2:domain2", "3:case3:domain2,domain3");
	}
}
