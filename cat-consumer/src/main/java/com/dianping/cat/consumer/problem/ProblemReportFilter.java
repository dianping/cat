package com.dianping.cat.consumer.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;

public class ProblemReportFilter extends BaseVisitor {

	private int m_maxUrlSize = 100;

	public ProblemReportFilter() {

	}

	public ProblemReportFilter(int size) {
		m_maxUrlSize = size;
	}

	@Override
	public void visitMachine(Machine machine) {
		Collection<Entity> entities = machine.getEntities().values();
		List<Entity> longUrls = new ArrayList<Entity>();
		List<Entity> errorCodes = new ArrayList<Entity>();

		for (Entity e : entities) {
			String status = e.getStatus();
			int length = status.length();

			for (int i = 0; i < length; i++) {
				// invalidate char
				if (status.charAt(i) > 126 || status.charAt(i) < 32) {
					errorCodes.add(e);
					break;
				}
			}

			if (ProblemType.LONG_URL.getName().equals(e.getType())) {
				longUrls.add(e);
			}
		}

		for (int i = 0; i < errorCodes.size(); i++) {
			entities.remove(errorCodes.get(i));
		}

		int size = longUrls.size();

		if (size > m_maxUrlSize) {
			for (int i = m_maxUrlSize; i < size; i++) {
				entities.remove(longUrls.get(i));
			}
		}
	}
}
