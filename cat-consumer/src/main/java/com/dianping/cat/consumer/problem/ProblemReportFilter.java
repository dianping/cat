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

		for (Entity e : entities) {
			String status = e.getStatus();
			StringBuilder sb = new StringBuilder();
			int length = status.length();
			char c;
			String s = "";

			for (int i = 0; i < length; i++) {
				c = status.charAt(i);

				if (c > 126 || c < 32) {
					s = " ";
				} else {
					s = String.valueOf(c);
				}
				sb.append(s);
			}
			e.setStatus(sb.toString().replaceAll(" +", " "));
			e.setId(e.getType() + ":" + e.getStatus());

			if (ProblemType.LONG_URL.getName().equals(e.getType())) {
				longUrls.add(e);
			}
		}

		int size = longUrls.size();

		if (size > m_maxUrlSize) {
			for (int i = m_maxUrlSize; i < size; i++) {
				entities.remove(longUrls.get(i));
			}
		}
	}
}
