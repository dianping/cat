package com.dianping.cat.abtest.repository;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.transform.DefaultSaxParser;
import com.dianping.cat.abtest.repository.HttpABTestEntityRepository.ABTestVisitor;
import com.dianping.cat.abtest.spi.ABTestEntity;

public class ABTestRepositoryTest extends ComponentTestCase {

	@Test
	public void testVisitor() throws Exception {
		HttpABTestEntityRepository repository = (HttpABTestEntityRepository) lookup(ABTestEntityRepository.class);
		String content = Files.forIO().readFrom(getClass().getResourceAsStream("abtest.xml"), "utf-8");

		ABTestVisitor visitor = repository.new ABTestVisitor("TuanGouWeb");
		AbtestModel abtest = DefaultSaxParser.parse(content);
		visitor.visitAbtestModel(abtest);

		Map<String, ABTestEntity> entities = visitor.getEntities();
		ABTestEntity mock = entities.get("mock");

		Assert.assertEquals(1, entities.size());
		Assert.assertNotNull(mock.getInvocable());
		Assert.assertNotNull(mock.getGroupStrategy());
	}
}
