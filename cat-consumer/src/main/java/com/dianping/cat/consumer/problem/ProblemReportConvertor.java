package com.dianping.cat.consumer.problem;

import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;

public class ProblemReportConvertor extends BaseVisitor {

	@Override
	public void visitMachine(Machine machine) {
		Map<String, Entity> entities = machine.getEntities();
		List<Entry> entries = machine.getEntries();

		if (entities.isEmpty() && !entries.isEmpty()) {
			for (Entry entry : entries) {
				String type = entry.getType();
				String status = entry.getStatus();
				String id = type + ":" + status;
				Entity entity = machine.findOrCreateEntity(id);

				entity.setType(type).setStatus(status);
				for (Duration duration : entry.getDurations().values()) {
					entity.addDuration(duration);
				}

				for (JavaThread thread : entry.getThreads().values()) {
					entity.addThread(thread);
				}
			}
			entries.clear();
		}
	}
}
