package com.dianping.cat.server;

import java.util.List;
import java.util.Map;

public interface MetricService {

	public boolean insert(List<MetricEntity> entities);

	public Map<Long, Double> query(QueryParameter parameter);

	public List<ServerGroupByEntity> queryByFields(QueryParameter parameter);

	public List<String> queryEndPoints(String category);

	public List<String> queryEndPoints(String category, String tag, List<String> keywords);

	public List<String> queryEndPointsByTag(String category, List<String> tags);

	public List<String> queryMeasurements(String category);

	public List<String> queryMeasurements(String category, List<String> endPoints);

	public List<String> queryMeasurements(String category, String measurement, List<String> endPoints);

	public List<String> queryTagValues(String category, String measurement, String tag);

}
