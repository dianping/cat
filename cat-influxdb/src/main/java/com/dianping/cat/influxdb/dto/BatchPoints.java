package com.dianping.cat.influxdb.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.dianping.cat.influxdb.InfluxDB.ConsistencyLevel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * {Purpose of This Type}
 *
 * {Other Notes Relating to This Type (Optional)}
 *
 * @author stefan
 *
 */
public class BatchPoints {
	private String database;
	private String retentionPolicy;
	private Map<String, String> tags;
	private List<Point> points;
	private ConsistencyLevel consistency;

	BatchPoints() {
		// Only visible in the Builder
	}

	/**
	 * Create a new BatchPoints build to create a new BatchPoints in a fluent manner-
	 *
	 * @param database
	 *            the name of the Database
	 * @return the Builder to be able to add further Builder calls.
	 */
	public static Builder database(final String database) {
		return new Builder(database);
	}

	/**
	 * The Builder to create a new BatchPoints instance.
	 */
	public static final class Builder {
		private final String database;
		private String retentionPolicy;
		private final Map<String, String> tags = Maps.newTreeMap(Ordering.natural());
		private final List<Point> points = Lists.newArrayList();
		private ConsistencyLevel consistency;

		/**
		 * @param database
		 */
		Builder(final String database) {
			this.database = database;
		}

		/**
		 * The retentionPolicy to use.
		 *
		 * @param policy
		 * @return the Builder instance
		 */
		public Builder retentionPolicy(final String policy) {
			this.retentionPolicy = policy;
			return this;
		}

		/**
		 * Add a tag to this set of points.
		 *
		 * @param tagName
		 *            the tag name
		 * @param value
		 *            the tag value
		 * @return the Builder instance.
		 */
		public Builder tag(final String tagName, final String value) {
			this.tags.put(tagName, value);
			return this;
		}

		/**
		 * Add a Point to this set of points.
		 *
		 * @param pointToAdd
		 * @return the Builder instance
		 */
		public Builder point(final Point pointToAdd) {
			this.points.add(pointToAdd);
			return this;
		}

		/**
		 * Add a set of Points to this set of points.
		 *
		 * @param pointsToAdd
		 * @return the Builder instance
		 */
		public Builder points(final Point... pointsToAdd) {
			this.points.addAll(Arrays.asList(pointsToAdd));
			return this;
		}

		/**
		 * Set the ConsistencyLevel to use. If not given it defaults to {@link ConsistencyLevel#ONE}
		 *
		 * @param consistencyLevel
		 * @return the Builder instance
		 */
		public Builder consistency(final ConsistencyLevel consistencyLevel) {
			this.consistency = consistencyLevel;
			return this;
		}

		/**
		 * Create a new BatchPoints instance.
		 *
		 * @return the created BatchPoints.
		 */
		public BatchPoints build() {
			Preconditions.checkArgument(!Strings.isNullOrEmpty(this.database), "Database must not be null or empty.");
			BatchPoints batchPoints = new BatchPoints();
			batchPoints.setDatabase(this.database);
			for (Point point : this.points) {
				point.getTags().putAll(this.tags);
			}
			batchPoints.setPoints(this.points);
			batchPoints.setRetentionPolicy(this.retentionPolicy);
			batchPoints.setTags(this.tags);
			if (null == this.consistency) {
				this.consistency = ConsistencyLevel.ONE;
			}
			batchPoints.setConsistency(this.consistency);
			return batchPoints;
		}
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return this.database;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	void setDatabase(final String database) {
		this.database = database;
	}

	/**
	 * @return the retentionPolicy
	 */
	public String getRetentionPolicy() {
		return this.retentionPolicy;
	}

	/**
	 * @param retentionPolicy
	 *            the retentionPolicy to set
	 */
	void setRetentionPolicy(final String retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}

	/**
	 * @return the points
	 */
	public List<Point> getPoints() {
		return this.points;
	}

	/**
	 * @param points
	 *            the points to set
	 */
	void setPoints(final List<Point> points) {
		this.points = points;
	}

	/**
	 * Add a single Point to these batches.
	 *
	 * @param point
	 * @return this Instance to be able to daisy chain calls.
	 */
	public BatchPoints point(final Point point) {
		point.getTags().putAll(this.tags);
		this.points.add(point);
		return this;
	}

	/**
	 * @return the tags
	 */
	public Map<String, String> getTags() {
		return this.tags;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	void setTags(final Map<String, String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the consistency
	 */
	public ConsistencyLevel getConsistency() {
		return this.consistency;
	}

	/**
	 * @param consistency
	 *            the consistency to set
	 */
	void setConsistency(final ConsistencyLevel consistency) {
		this.consistency = consistency;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BatchPoints [database=");
		builder.append(this.database);
		builder.append(", retentionPolicy=");
		builder.append(this.retentionPolicy);
		builder.append(", tags=");
		builder.append(this.tags);
		builder.append(", points=");
		builder.append(this.points);
		builder.append("]");
		return builder.toString();
	}

	// measurement[,tag=value,tag2=value2...] field=value[,field2=value2...] [unixnano]
	/**
	 * calculate the lineprotocol for all Points.
	 *
	 * @return the String with newLines.
	 */
	public String lineProtocol() {
		StringBuilder sb = new StringBuilder();
		for (Point point : this.points) {
			sb.append(point.lineProtocol()).append("\n");
		}
		return sb.toString();
	}
}
