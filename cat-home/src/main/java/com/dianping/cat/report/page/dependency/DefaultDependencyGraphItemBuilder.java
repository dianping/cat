package com.dianping.cat.report.page.dependency;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.home.dependency.entity.Edge;
import com.dianping.cat.home.dependency.entity.Node;

public class DefaultDependencyGraphItemBuilder implements DependendencyGraphItemBuilder {

	public static final String PROJECT = "project";
	public static final String DATABASE = "database";

	public static int OK = 1;

	public static int WARN = 2;

	public static int ERROR = 3;

	@Override
	public Node buildNode(String domain, Index index) {
		Node node = new Node(domain);

		node.setStatus(OK);
		node.setType(PROJECT);
		node.setWeight(1);
		node.setDes("");
		node.setLink("");
		return node;
	}

	@Override
	public Edge buildEdge(String domain, Dependency dependency) {
		Edge edge = new Edge();

		edge.setType(dependency.getType());
		edge.setKey(dependency.getType() + ':' + domain + ':' + dependency.getTarget());
		edge.setSelf(domain);
		edge.setTarget(dependency.getTarget());
		edge.setOpposite(false);
		edge.setWeight(1);
		edge.setStatus(OK);
		edge.setDes("");
		edge.setLink("");
		return edge;
	}

	@Override
   public Node buildDatabaseNode(String database) {
		Node node = new Node(database);

		node.setStatus(OK);
		node.setType(DATABASE);
		node.setWeight(1);
		node.setDes("");
		node.setLink("");
		return node;
   }

}
