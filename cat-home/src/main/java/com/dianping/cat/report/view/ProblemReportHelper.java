package com.dianping.cat.report.view;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Segment;

public class ProblemReportHelper {
	public static String showLegends(JavaThread thread, int minute) {
		Segment segment = thread.findSegment(minute);

		if (segment == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder(256);
		Map<String, String> map = new HashMap<String, String>();

		for (Entry entry : segment.getEntries()) {
			String type = entry.getType();

			if (!map.containsKey(type)) {
				map.put(type, entry.getMessageId());
			}
		}

		for (ProblemType type : ProblemType.values()) {
			String name = type.getName();
			String messageId = map.get(name);

			if (messageId == null) {
				sb.append("&nbsp;&nbsp;");
			} else {
				sb.append("<a href=\"/cat/r/m/").append(messageId).append("/logview.html\" class=\"");
				sb.append(name);
				sb.append("\">&nbsp;&nbsp;</a>");
			}
		}

		return sb.toString();
	}
}
