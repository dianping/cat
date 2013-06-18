package com.dianping.abtest.sample;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Splitters;

import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

/**
 * @author hao.zhu
 */
public class RetinaImgGroupStrategy implements ABTestGroupStrategy, LogEnabled {
	public static final String ID = "retina_img_groupstrategy";

	private Set<String> m_dealIds = new HashSet<String>();

	private AtomicInteger counter = new AtomicInteger(0);

	private int m_percentage;

	private Logger m_logger;

	@Override
	public void apply(ABTestContext ctx) {
		HttpServletRequest request = ctx.getHttpServletRequest();
		String dealId = request.getParameter("dealGroupId");

		if (m_dealIds.contains(dealId)) {
			int count = counter.incrementAndGet();

			if (count % 100 <= m_percentage) {
				ctx.setGroupName("A");
			} else {
				ctx.setGroupName(ABTestContext.DEFAULT_GROUP);
			}
		}
	}

	@Override
	public void init(ABTestEntity entity) {
		String configuration = entity.getGroupStrategyConfiguration();
		List<String> lines = Splitters.by('\n').noEmptyItem().trim().split(configuration);
		
		for (String line : lines) {
			if (line.startsWith("#")) { // comment line
				continue;
			}

			int pos = line.indexOf('=');

			if (pos > 0) {
				String name = line.substring(0, pos).trim();
				String value = line.substring(pos + 1).trim();

				if ("percentage".equals(name)) {
					try {
						m_percentage = Integer.parseInt(value);
					} catch (Exception e) {
						m_logger.warn(String.format("Invalid percentage(%s) found!", value), e);
					}
				} else if ("deals".equals(name)) {
					List<String> parts = Splitters.by(',').noEmptyItem().trim().split(value);

					m_dealIds.addAll(parts);
				}
			} else {
				m_logger.warn(String.format("Invalid configuration line(%s) found!", line));
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
