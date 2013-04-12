package com.dianping.cat.abtest.spi.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestContextManager;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestEntityManager;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class DefaultABTestContextManager extends ContainerHolder implements ABTestContextManager {
	@Inject
	private ABTestEntityManager m_entityManager;

	private InheritableThreadLocal<Entry> m_threadLocal = new InheritableThreadLocal<Entry>() {
		@Override
		protected Entry initialValue() {
			return new Entry();
		}
	};

	@Override
	public ABTestContext getContext(ABTestId testId) {
		Entry entry = m_threadLocal.get();
		Map<Integer, DefaultABTestContext> map = entry.getContextMap();
		int id = testId.getValue();
		DefaultABTestContext ctx = map.get(id);

		// 如果ctx不存在，返回默认的DefaultABTestContext
		if (ctx == null) {
			ctx = new DefaultABTestContext();
			// 把ctx保存起来, 如果在同一个请球内多次调用该方法，则都返回该DefaultABTestContext
			map.put(id, ctx);
		}

		return ctx;
	}

	@Override
	public void onRequestEnd() {
		m_threadLocal.remove();
	}

	@Override
	public void onRequestBegin(HttpServletRequest req) {
		Entry entry = m_threadLocal.get();
		Map<Integer, DefaultABTestContext> map = entry.getContextMap();

		// 获取所有的ABTestEntity
		Map<Integer, ABTestEntity> entities = m_entityManager.getEntities();
		// 遍历所有Entity，每个Entity都计算其分流结果，并设置到ctx中
		for (ABTestEntity entity : entities.values()) {
			DefaultABTestContext ctx = new DefaultABTestContext(entity);

			ctx.setup(req);

			if (!entity.isDisabled()) {
				if (entity.isEligible(new Date())) {
					Transaction t = Cat.newTransaction("GroupStrategy", entity.getGroupStrategy());
					ABTestGroupStrategy groupStrategy = lookup(ABTestGroupStrategy.class, entity.getGroupStrategy());
					try {
						// 计算分流结果
						groupStrategy.apply(ctx);

						t.setStatus(Message.SUCCESS);
					} catch (Throwable e) {
						t.setStatus(e);
						Cat.logError(e);
					} finally {
						t.complete();
					}
				}
			}
			// 将ctx保存到当前的Threadlocal中
			map.put(entity.getId(), ctx);
		}

	}

	static class Entry {
		private Map<Integer, DefaultABTestContext> m_map = new HashMap<Integer, DefaultABTestContext>(4);

		public Map<Integer, DefaultABTestContext> getContextMap() {
			return m_map;
		}

	}
}
