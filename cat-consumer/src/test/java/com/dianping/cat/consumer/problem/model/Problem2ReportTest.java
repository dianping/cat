package com.dianping.cat.consumer.problem.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.junit.Test;

import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.consumer.problem2.model.entity.Duration;
import com.dianping.cat.consumer.problem2.model.entity.Entry;
import com.dianping.cat.consumer.problem2.model.entity.JavaThread;
import com.dianping.cat.consumer.problem2.model.entity.Machine;
import com.dianping.cat.consumer.problem2.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem2.model.entity.Segment;
import com.site.helper.Files;

public class Problem2ReportTest {
	@Test
	public void testXml() throws Exception {
		String source = Files.forIO().readFrom(new File("/Users/qmwu/Downloads/LAST.xml"), "utf-8");
		com.dianping.cat.consumer.problem.model.entity.ProblemReport p1 = com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser
		      .parse(source);

		ProblemReport p2 = new ProblemReport(p1.getDomain());
		ProblemConverter converter = new ProblemConverter(p2);

		p1.accept(converter);

		System.out.println("old size: " + p1.toString().length());
		System.out.println("new size: " + p2.toString().length());

		// junit.framework.Assert.assertEquals(p1.toString(), p2.toString());
	}

	static class ProblemConverter extends BaseVisitor {
		private Stack<Object> m_stack = new Stack<Object>();

		private Map<String, Object> m_map = new HashMap<String, Object>();

		public ProblemConverter(ProblemReport root) {
			m_stack.push(root);
		}

		@Override
		public void visitEntry(com.dianping.cat.consumer.problem.model.entity.Entry entry) {
			String type = entry.getType();
			String status = entry.getStatus();
			int duration = entry.getDuration();
			String messageId = entry.getMessageId();

			Entry e = (Entry) get("entry", type + ":" + status);

			if (e == null) {
				Machine parent = (Machine) m_stack.peek();

				e = new Entry().setType(type).setStatus(status);
				parent.addEntry(e);
				set("entry", type + ":" + status, e);
			}

			if (duration > 1000) {
				duration = duration - duration % 1000;
			} else {
				duration = duration - duration % 100;
			}

			Duration d = e.findOrCreateDuration(duration);

			d.incCount();

			boolean limitedMessage = true;

			if (!limitedMessage || limitedMessage && d.getMessages().size() < 60) {
				d.addMessage(messageId);
			}

			boolean needThread = false;

			if (needThread) {
				JavaThread t = e.findOrCreateThread((String) get("thread", "id"));
				Segment s = t.findOrCreateSegment((Integer) get("segment", "id"));

				if (t.getGroupName() == null) {
					t.setGroupName((String) get("thread", "groupName"));
					t.setName((String) get("thread", "name"));
				}

				s.incCount();
				s.addMessage(messageId);
			}
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.problem.model.entity.Machine machine) {
			ProblemReport parent = (ProblemReport) m_stack.peek();
			Machine m = new Machine();

			m.setIp(machine.getIp());
			parent.addMachine(m);

			m_stack.push(m);
			super.visitMachine(machine);
			m_stack.pop();
		}

		@Override
		public void visitProblemReport(com.dianping.cat.consumer.problem.model.entity.ProblemReport problemReport) {
			ProblemReport root = (ProblemReport) m_stack.peek();

			root.setStartTime(problemReport.getStartTime());
			root.setEndTime(problemReport.getEndTime());
			root.getDomainNames().addAll(problemReport.getDomainNames());
			root.getIps().addAll(problemReport.getIps());

			super.visitProblemReport(problemReport);
		}

		@Override
		public void visitSegment(com.dianping.cat.consumer.problem.model.entity.Segment segment) {
			set("segment", "id", segment.getId());

			super.visitSegment(segment);
		}

		@Override
		public void visitThread(com.dianping.cat.consumer.problem.model.entity.JavaThread thread) {
			set("thread", "groupName", thread.getGroupName());
			set("thread", "name", thread.getName());
			set("thread", "id", thread.getId());

			super.visitThread(thread);
		}

		@SuppressWarnings("unchecked")
		private <T> T get(String group, String key) {
			return (T) m_map.get(group + ":" + key);
		}

		private void set(String group, String key, Object value) {
			m_map.put(group + ":" + key, value);
		}
	}
}
