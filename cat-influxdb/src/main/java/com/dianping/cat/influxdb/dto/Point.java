package com.dianping.cat.influxdb.dto;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

/**
 * Representation of a InfluxDB database Point.
 * 
 * @author stefan.majer [at] gmail.com
 * 
 */
public class Point {
	private String measurement;
	private Map<String, String> tags;
	private Long time;
	private TimeUnit precision = TimeUnit.NANOSECONDS;
	private Map<String, Object> fields;

	private static final Escaper FIELD_ESCAPER = Escapers.builder().addEscape('"', "\\\"").build();
	private static final Escaper KEY_ESCAPER = Escapers.builder().addEscape(' ', "\\ ").addEscape(',', "\\,").build();

	Point() {
	}

	/**
	 * Create a new Point Build build to create a new Point in a fluent manner-
	 *
	 * @param measurement
	 *            the name of the measurement.
	 * @return the Builder to be able to add further Builder calls.
	 */

	public static Builder measurement(final String measurement) {
		return new Builder(measurement);
	}

	/**
	 * Builder for a new Point.
	 *
	 * @author stefan.majer [at] gmail.com
	 *
	 */
	public static final class Builder {
		private final String measurement;
		private final Map<String, String> tags = Maps.newTreeMap(Ordering.natural());
		private Long time;
		private TimeUnit precision = TimeUnit.NANOSECONDS;
		private final Map<String, Object> fields = Maps.newTreeMap(Ordering.natural());

		/**
		 * @param measurement
		 */
		Builder(final String measurement) {
			this.measurement = measurement;
		}

		/**
		 * Add a tag to this point.
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
		 * Add a Map of tags to add to this point.
		 *
		 * @param tagsToAdd
		 *            the Map of tags to add
		 * @return the Builder instance.
		 */
		public Builder tag(final Map<String, String> tagsToAdd) {
			this.tags.putAll(tagsToAdd);
			return this;
		}

		/**
		 * Add a field to this point.
		 *
		 * @param field
		 *            the field name
		 * @param value
		 *            the value of this field
		 * @return the Builder instance.
		 */
		public Builder field(final String field, final Object value) {
			this.fields.put(field, value);
			return this;
		}

		/**
		 * Add a Map of fields to this point.
		 *
		 * @param fieldsToAdd
		 *            the fields to add
		 * @return the Builder instance.
		 */
		public Builder fields(final Map<String, Object> fieldsToAdd) {
			this.fields.putAll(fieldsToAdd);
			return this;
		}

		/**
		 * Add a time to this point
		 *
		 * @param precisionToSet
		 * @param timeToSet
		 * @return the Builder instance.
		 */
		public Builder time(final long timeToSet, final TimeUnit precisionToSet) {
		    Preconditions.checkNotNull(precisionToSet, "Precision must be not null!");
			this.time = timeToSet;
			this.precision = precisionToSet;
			return this;
		}

		/**
		 * Create a new Point.
		 *
		 * @return the newly created Point.
		 */
		public Point build() {
			Preconditions
					.checkArgument(!Strings.isNullOrEmpty(this.measurement), "Point name must not be null or empty.");
			Preconditions.checkArgument(this.fields.size() > 0, "Point must have at least one field specified.");
			Point point = new Point();
			point.setFields(this.fields);
			point.setMeasurement(this.measurement);
			if (this.time != null) {
			    point.setTime(this.time);
			    point.setPrecision(this.precision);
			} else {
			    point.setTime(System.currentTimeMillis());
			    point.setPrecision(TimeUnit.MILLISECONDS);
			}
			point.setTags(this.tags);
			return point;
		}
	}

	/**
	 * @param measurement
	 *            the measurement to set
	 */
	void setMeasurement(final String measurement) {
		this.measurement = measurement;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	void setTime(final Long time) {
		this.time = time;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	void setTags(final Map<String, String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the tags
	 */
	Map<String, String> getTags() {
		return this.tags;
	}

	/**
	 * @param precision
	 *            the precision to set
	 */
	void setPrecision(final TimeUnit precision) {
		this.precision = precision;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	void setFields(final Map<String, Object> fields) {
		this.fields = fields;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Point [name=");
		builder.append(this.measurement);
		builder.append(", time=");
		builder.append(this.time);
		builder.append(", tags=");
		builder.append(this.tags);
		builder.append(", precision=");
		builder.append(this.precision);
		builder.append(", fields=");
		builder.append(this.fields);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * calculate the lineprotocol entry for a single Point.
	 * 
	 * Documentation is WIP : https://github.com/influxdb/influxdb/pull/2997
	 * 
	 * https://github.com/influxdb/influxdb/blob/master/tsdb/README.md
	 *
	 * @return the String without newLine.
	 */
	public String lineProtocol() {
		final StringBuilder sb = new StringBuilder();
		sb.append(KEY_ESCAPER.escape(this.measurement));
		sb.append(concatenatedTags());
		sb.append(concatenateFields());
		sb.append(formatedTime());
		return sb.toString();
	}

	private StringBuilder concatenatedTags() {
		final StringBuilder sb = new StringBuilder();
		for (Entry<String, String> tag : this.tags.entrySet()) {
			sb.append(",");
			sb.append(KEY_ESCAPER.escape(tag.getKey())).append("=").append(KEY_ESCAPER.escape(tag.getValue()));
		}
		sb.append(" ");
		return sb;
	}

	private StringBuilder concatenateFields() {
		final StringBuilder sb = new StringBuilder();
		final int fieldCount = this.fields.size();
		int loops = 0;

		NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
		numberFormat.setMaximumFractionDigits(340);
		numberFormat.setGroupingUsed(false);
		numberFormat.setMinimumFractionDigits(1);

		for (Entry<String, Object> field : this.fields.entrySet()) {
			sb.append(KEY_ESCAPER.escape(field.getKey())).append("=");
			loops++;
			Object value = field.getValue();
			if (value instanceof String) {
				String stringValue = (String) value;
				sb.append("\"").append(FIELD_ESCAPER.escape(stringValue)).append("\"");
			} else if (value instanceof Number) {
				sb.append(numberFormat.format(value));
			} else {
				sb.append(value);
			}

			if (loops < fieldCount) {
				sb.append(",");
			}
		}
		return sb;
	}

	private StringBuilder formatedTime() {
		final StringBuilder sb = new StringBuilder();
		if (null == this.time) {
			this.time = System.nanoTime();
		}
		sb.append(" ").append(TimeUnit.NANOSECONDS.convert(this.time, this.precision));
		return sb;
	}

}
