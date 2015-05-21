package com.dianping.cat.report.graph.svg;

public interface GraphBuilder {
	public String build(GraphPayload payload);

	public void setGraphType(int GraphType);
}
