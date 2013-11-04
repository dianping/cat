package com.dianping.cat.abtest.spi.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.ABTestName;
import com.dianping.cat.abtest.repository.ABTestEntityRepository;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultABTestEntityManager extends ContainerHolder implements ABTestEntityManager, Initializable {
	@Inject
	private ABTestEntityRepository m_repository;

	@Inject
	private MessageManager m_messageManager;

	@Inject
	private ABTestCodec m_cookieCodec;

	@Override
	public ABTestEntity getEntity(ABTestName name) {
		String id = name.getValue();
		ABTestEntity entity = m_repository.getCurrentEntities().get(id);

		if (entity == null) {
			entity = new ABTestEntity();
			entity.setName(id);
			entity.setDisabled(true);

			m_repository.getCurrentEntities().put(id, entity);

			Cat.getProducer().logEvent("ABTestDisabled", id, Message.SUCCESS, null);
		}

		return entity;
	}

	public List<ABTestEntity> getEntityList() {
		List<ABTestEntity> entitiesList = new ArrayList<ABTestEntity>();

		for (ABTestEntity entity : m_repository.getCurrentEntities().values()) {
			entity.setMessageManager(m_messageManager);
			entity.setCookieCodec(m_cookieCodec);

			entitiesList.add(entity);
		}

		return entitiesList;
	}

	public Set<String> getActiveRun() {
		return m_repository.getActiveRuns();
	}

	@Override
	public void initialize() throws InitializationException {
		for (ABTestEntity entity : m_repository.getCurrentEntities().values()) {
			entity.setMessageManager(m_messageManager);
			entity.setCookieCodec(m_cookieCodec);
		}
	}
}