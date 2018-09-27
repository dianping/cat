package com.dianping.cat.influxdb.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.InfluxDB.ConsistencyLevel;
import com.dianping.cat.influxdb.dto.BatchPoints;
import com.dianping.cat.influxdb.dto.Point;
import com.dianping.cat.influxdb.dto.Query;
import com.dianping.cat.influxdb.dto.QueryResult;
import com.dianping.cat.influxdb.dto.QueryResult.Result;
import com.dianping.cat.influxdb.dto.QueryResult.Series;
import com.dianping.cat.server.DataSourceService;
import com.dianping.cat.server.MetricEntity;
import com.dianping.cat.server.MetricService;
import com.dianping.cat.server.QueryParameter;
import com.dianping.cat.server.ServerGroupByEntity;

@Named(type = MetricService.class, value = InfluxDB.ID)
public class MetricServiceImpl implements MetricService, LogEnabled {

	@Inject(InfluxDB.ID)
	private DataSourceService<InfluxDBConnection> m_dataSourceService;

	private Logger m_logger;

	private Map<String, List<MetricEntity>> buildCategories(List<MetricEntity> entities) {
		Map<String, List<MetricEntity>> categories = new LinkedHashMap<String, List<MetricEntity>>();

		for (MetricEntity entity : entities) {
			String category = entity.getCategory();
			List<MetricEntity> list = categories.get(category);

			if (list == null) {
				list = new LinkedList<MetricEntity>();

				categories.put(category, list);
			}
			list.add(entity);
		}
		return categories;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public boolean insert(List<MetricEntity> entities) {
		boolean ret = true;
		Map<String, List<MetricEntity>> categories = buildCategories(entities);

		for (Entry<String, List<MetricEntity>> entry : categories.entrySet()) {
			try {
				InfluxDBConnection conn = m_dataSourceService.getConnection(entry.getKey());
				BatchPoints batchPoints = BatchPoints.database(conn.getDataBase()).retentionPolicy("default")
				      .consistency(ConsistencyLevel.ALL).build();

				for (MetricEntity entity : entry.getValue()) {
					Point point = Point.measurement(entity.getMeasure()).tag(entity.getTags())
					      .time(entity.getTimestamp(), TimeUnit.MILLISECONDS).fields(entity.getFields()).build();
					batchPoints.point(point);
				}

				conn.getInfluxDB().write(batchPoints);
			} catch (Exception e) {
				ret = ret && false;

				Cat.logError(e);
			}
		}
		return ret;
	}

	private List<String> parseData(QueryResult result, int index) {
		List<String> results = new ArrayList<String>();

		if (result != null && result.getResults() != null) {
			for (Result r : result.getResults()) {
				List<Series> series = r.getSeries();

				if (series != null) {
					for (Series s : series) {
						if (s != null && s.getValues() != null) {
							for (List<Object> v : s.getValues()) {
								results.add(String.valueOf(v.get(index)));
							}
						}
					}
				}
			}
		}
		return results;
	}

	@Override
	public Map<Long, Double> query(QueryParameter parameter) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(parameter.getCategory());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (conn != null) {
			String format = "SELECT %s(value) FROM \"%s\" WHERE %s TIME >= '%s' AND TIME < '%s' GROUP BY time(%s) fill(%s)";
			String query = String.format(format, parameter.getType().getName(), parameter.getMeasurement(),
			      parameter.getSqlTags(), sdf.format(parameter.getStart()), sdf.format(parameter.getEnd()),
			      parameter.getInterval(), parameter.getFillValue());
			QueryResult queryResult = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			Map<Long, Double> datas = new LinkedHashMap<Long, Double>();

			if (queryResult != null && queryResult.getResults() != null) {
				for (Result r : queryResult.getResults()) {
					List<Series> series = r.getSeries();

					if (series != null) {
						for (Series s : series) {
							if (s != null && s.getValues() != null) {
								for (List<Object> v : s.getValues()) {
									try {
										Date date = sdf.parse(String.valueOf(v.get(0)));
										double data = (double) (v.get(1));

										datas.put(date.getTime(), data);
									} catch (Exception e) {
										Cat.logError(e);
										m_logger.error("error parse influx query:" + query + v.get(0) + ":" + v.get(1));
									}
								}
							}
						}
					}
				}
			}
			return datas;
		} else {
			return Collections.emptyMap();
		}
	}

	@Override
	public List<ServerGroupByEntity> queryByFields(QueryParameter parameter) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(parameter.getCategory());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (conn != null) {
			String format = "SELECT %s(value) FROM %s WHERE %s TIME >= '%s' AND TIME < '%s' GROUP BY %s time(%s) fill(%s)";
			String query = String.format(format, parameter.getType().getName(), parameter.getMeasurement(),
			      parameter.getSqlTags(), sdf.format(parameter.getStart()), sdf.format(parameter.getEnd()),
			      parameter.getGroupBy(), parameter.getInterval(), parameter.getFillValue());
			QueryResult queryResult = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<ServerGroupByEntity> entities = new LinkedList<ServerGroupByEntity>();

			if (queryResult != null && queryResult.getResults() != null) {
				for (Result r : queryResult.getResults()) {
					List<Series> series = r.getSeries();

					if (series != null) {
						for (Series s : series) {
							String measure = s.getName();
							String endPoint = buildEndPoint(s);
							Map<Long, Double> values = new LinkedHashMap<Long, Double>();
							ServerGroupByEntity entity = new ServerGroupByEntity(measure, endPoint, values);

							entities.add(entity);

							if (s != null && s.getValues() != null) {
								for (List<Object> v : s.getValues()) {
									try {
										Date date = sdf.parse(String.valueOf(v.get(0)));
										double data = (double) (v.get(1));

										values.put(date.getTime(), data);
									} catch (Exception e) {
										Cat.logError(e);
										m_logger.error("error parse influx query:" + query + v.get(0) + ":" + v.get(1));
									}
								}
							}
						}
					}
				}
			}
			return entities;
		} else {
			return Collections.emptyList();
		}
	}

	private String buildEndPoint(Series s) {
		StringBuilder sb = new StringBuilder();
		String result = "";

		for (Entry<String, String> entry : s.getTags().entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
		}

		if (sb.length() > 2) {
			result = sb.toString().substring(0, sb.length() - 2);
		}
		return result;
	}

	@Override
	public List<String> queryEndPoints(String category) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			String format = "SHOW TAG VALUES FROM  /^%s.*/  WITH KEY = \"endPoint\"";
			String query = String.format(format, category);
			QueryResult result = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<String> results = removeDuplicates(parseData(result, 1));

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryEndPoints(String category, String tag, List<String> keywords) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			String format = "SHOW TAG VALUES FROM  /.*/  WITH KEY = \"endPoint\" WHERE %s =~ /.*%s.*/";
			String query = String.format(format, tag, StringUtils.join(keywords, "|"));
			QueryResult result = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<String> results = removeDuplicates(parseData(result, 1));

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryEndPointsByTag(String category, List<String> tags) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			String format = "SHOW TAG VALUES FROM  /.*/  WITH KEY = \"endPoint\" WHERE %s";
			String query = String.format(format, StringUtils.join(tags, " AND "));
			QueryResult result = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<String> results = removeDuplicates(parseData(result, 1));

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryMeasurements(String category) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			QueryResult result = conn.getInfluxDB().query(new Query("SHOW MEASUREMENTS", conn.getDataBase()));
			List<String> results = parseData(result, 0);

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryMeasurements(String category, List<String> endPoints) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			List<String> list = new ArrayList<String>();

			for (String endPoint : endPoints) {
				String query = "endPoint='" + endPoint + "'";

				list.add(query);
			}

			String format = "SHOW SERIES WHERE %s";
			String query = String.format(format, StringUtils.join(list, " OR "));
			QueryResult result = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<String> results = parseData(result, 0);

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryMeasurements(String category, String measurement, List<String> endPoints) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			List<String> list = new ArrayList<String>();

			for (String endPoint : endPoints) {
				String query = "endPoint='" + endPoint + "'";

				list.add(query);
			}

			String format = "SHOW SERIES FROM \"%s\" WHERE %s";
			String query = String.format(format, measurement, StringUtils.join(list, " OR "));
			QueryResult result = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<String> results = parseData(result, 0);

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryTagValues(String category, String measurement, String tag) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			String format = "SHOW TAG VALUES FROM \"%s\" WITH KEY='%s'";
			String query = String.format(format, measurement, tag);
			QueryResult result = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<String> results = removeDuplicates(parseData(result, 1));

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	private List<String> removeDuplicates(List<String> datas) {
		Set<String> hs = new HashSet<String>();

		hs.addAll(datas);
		return new ArrayList<String>(hs);
	}
}
