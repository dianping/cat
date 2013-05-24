package com.dianping.cat.report.page.dependency;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.home.dependency.entity.Edge;
import com.dianping.cat.home.dependency.entity.Node;

public interface DependendencyGraphItemBuilder {

	public Node buildNode(String domain, Index index);
	
	public Node buildDatabaseNode(String database);

	public Edge buildEdge(String domain, Dependency dependency);

}
