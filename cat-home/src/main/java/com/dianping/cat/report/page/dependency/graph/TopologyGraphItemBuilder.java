package com.dianping.cat.report.page.dependency.graph;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.home.dependency.graph.entity.Edge;
import com.dianping.cat.home.dependency.graph.entity.Node;

public class TopologyGraphItemBuilder {

	private Date m_start;

	public static final String PROJECT = "project";

	public static final String DATABASE = "database";

	public static int OK = 1;

	public static int WARN = 2;

	public static int ERROR = 3;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHH");

	private DecimalFormat m_df = new DecimalFormat("0.0");

	public Node buildDatabaseNode(String database) {
		Node node = new Node(database);

		node.setStatus(OK);
		node.setType(DATABASE);
		node.setWeight(1);
		node.setDes(database);
		node.setLink("");
		return node;
	}

	public Edge buildEdge(String domain, Dependency dependency) {
		Edge edge = new Edge();

		edge.setType(dependency.getType());
		edge.setKey(dependency.getType() + ':' + domain + ':' + dependency.getTarget());
		edge.setSelf(domain);
		edge.setTarget(dependency.getTarget());
		edge.setOpposite(false);
		edge.setWeight(1);
		edge.setStatus(OK);
		StringBuilder sb = new StringBuilder(dependency.getType());

		if (dependency.getErrorCount() > 0) {
			sb.append(" Error:" + dependency.getErrorCount());
			sb.append(" Avg:" + m_df.format(dependency.getAvg()));
		}
		edge.setDes(sb.toString());
		edge.setLink(buildProblemLink(domain, m_start));
		return edge;
	}

	public Node createNode(String domain) {
		Node node = new Node(domain);

		node.setStatus(OK);
		node.setType(PROJECT);
		node.setWeight(1);
		node.setDes("");
		node.setLink(buildProblemLink(domain, m_start));
		return node;
	}
	
	public Node buildNode(String domain, Index index) {
		Node node = new Node(domain);

		node.setStatus(OK);
		node.setType(PROJECT);
		node.setWeight(1);
		StringBuilder sb = new StringBuilder();

		if (index.getErrorCount() > 0) {
			sb.append(" Error:" + index.getErrorCount());
		}
		node.setDes(sb.toString());
		node.setLink(buildProblemLink(domain, m_start));
		return node;
	}

	private String buildProblemLink(String domain, Date date) {
		return "p?domain=" + domain + "&date=" + m_sdf.format(date);
	}

	public TopologyGraphItemBuilder setDate(Date start) {
		m_start = start;
		return this;
	}

}
