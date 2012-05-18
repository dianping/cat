package com.dianping.cat.job.joblet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.job.joblet.LocationJoblet.Location;
import com.dianping.cat.job.joblet.LocationJoblet.LocationStat;
import com.dianping.cat.job.spi.JobCmdLine;
import com.dianping.cat.job.spi.joblet.Joblet;
import com.dianping.cat.job.spi.joblet.JobletContext;
import com.dianping.cat.job.spi.joblet.JobletMeta;
import com.dianping.cat.job.spi.mapreduce.MessageTreeWritable;
import com.dianping.cat.job.spi.mapreduce.PojoWritable;
import com.dianping.cat.job.sql.dal.LocationRecord;
import com.dianping.cat.job.sql.dal.LocationRecordDao;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.site.dal.jdbc.DalException;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

@JobletMeta(name = "location", description = "Location analysis", keyClass = Location.class, valueClass = LocationStat.class, combine = true, reducerNum = 1)
public class LocationJoblet extends ContainerHolder implements Joblet<Location, LocationStat> {
	private static final MessageFormat m_format = new MessageFormat("{2}?lat={0,number}&lng={1,number}&{3}");

	@Inject
	private LocationOutputter m_outputter;

	private Location getLocation(Transaction root) {
		List<Message> children = root.getChildren();

		for (Message child : children) {
			if (child instanceof Event && child.getType().equals("URL") && child.getName().equals("Payload")) {
				// URL:Payload
				// /location.bin?lat=31.20334&lng=121.58017&accuracy=1385
				String data = child.getData().toString();

				try {
					Object[] parts = m_format.parse(data);
					Number lat = (Number) parts[0];
					Number lng = (Number) parts[1];

					return new Location(lat.doubleValue(), lng.doubleValue(), root.getTimestamp());
				} catch (Exception e) {
					// ignore it
				}

				break;
			}
		}

		return null;
	}

	@Override
	public boolean initialize(JobCmdLine cmdLine) {
		String inputPath = cmdLine.getArg("inputPath", 0, null);
		String outputPath = cmdLine.getArg("outputPath", 1, null);

		if (inputPath != null) {
			cmdLine.setProperty("inputPath", inputPath);
		}

		if (outputPath != null) {
			cmdLine.setProperty("outputPath", outputPath);
		}

		String outputter = cmdLine.getProperty("outputter", null);

		if (outputter != null) {
			m_outputter = lookup(LocationOutputter.class, outputter);
		}

		return true;
	}

	@Override
	public void map(JobletContext context, MessageTreeWritable treeWritable) throws IOException, InterruptedException {
		MessageTree tree = treeWritable.get();
		Message root = tree.getMessage();
		final String type = root.getType();
		final String name = root.getName();

		if (root instanceof Transaction && type.equals("URL")
		      && (name.equals("/location.bin") || name.equals("/locationhd.bin"))) {
			Location location = getLocation((Transaction) root);

			if (location != null) {
				context.write(location, LocationStat.ONCE);
			}
		}
	}

	@Override
	public void reduce(JobletContext context, Location location, Iterable<LocationStat> stats) throws IOException,
	      InterruptedException {
		LocationStat all = new LocationStat();

		for (LocationStat stat : stats) {
			all.add(stat.getCount());
		}

		if (context.isInCombiner()) {
			context.write(location, all);
		} else {
			m_outputter.out(context, location, all);
		}
	}

	@Override
	public void summary() {
		System.out.println(m_outputter);
	}

	public static class Location extends PojoWritable {
		private double m_lat;

		private double m_lng;

		private long m_transactionDate;

		public Location() {
		}

		public Location(double lat, double lng, long transactionDate) {
			m_lat = lat;
			m_lng = lng;
			m_transactionDate = transactionDate;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (!(obj instanceof Location))
				return false;
			Location other = (Location) obj;
			if (Double.doubleToLongBits(m_lat) != Double.doubleToLongBits(other.m_lat))
				return false;
			if (Double.doubleToLongBits(m_lng) != Double.doubleToLongBits(other.m_lng))
				return false;
			if (m_transactionDate != other.m_transactionDate)
				return false;
			return true;
		}

		public double getLat() {
			return m_lat;
		}

		public double getLng() {
			return m_lng;
		}

		public long getTransactionDate() {
			return m_transactionDate;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			long temp;

			temp = Double.doubleToLongBits(m_lat);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(m_lng);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + (int) (m_transactionDate ^ (m_transactionDate >>> 32));

			return result;
		}
	}

	public static class LocationDatabaseDumper implements LocationOutputter, LogEnabled {
		@Inject
		private LocationRecordDao m_dao;

		private List<LocationRecord> m_records = new ArrayList<LocationRecord>();

		private int m_count;

		private Logger m_logger;

		@Override
		public void enableLogging(Logger logger) {
			m_logger = logger;
		}

		private void flushToDatabase() {
			if (m_records.size() > 0) {
				try {
					final LocationRecord[] batch = m_records.toArray(new LocationRecord[0]);

					m_dao.insert(batch);
					m_records.clear();
					m_count += batch.length;
					m_logger.info(String.format("%s records inserted.", m_count));
				} catch (DalException e) {
					m_logger.error("Error when batch inserting data to database.", e);
				}
			}
		}

		@Override
		public void out(JobletContext context, Location location, LocationStat stat) throws IOException,
		      InterruptedException {
			if (m_records.size() >= 100) {
				flushToDatabase();
			}

			LocationRecord record = m_dao.createLocal();

			record.setLat(location.getLat());
			record.setLng(location.getLng());
			record.setTransactionDate(new Date(location.getTransactionDate()));
			record.setTotal(stat.getCount());

			m_records.add(record);
		}

		@Override
		public String toString() {
			flushToDatabase();
			return "";
		}
	}

	public static interface LocationOutputter {
		public void out(JobletContext context, Location location, LocationStat all) throws IOException,
		      InterruptedException;
	}

	public static class LocationReporter implements LocationOutputter {
		private Map<Location, LocationStat> m_stats = new TreeMap<Location, LocationStat>();

		@Override
		public void out(JobletContext context, Location location, LocationStat stat) throws IOException,
		      InterruptedException {
			m_stats.put(location, stat);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(8192);
			boolean first = true;

			sb.append('[');

			for (Map.Entry<Location, LocationStat> e : m_stats.entrySet()) {
				Location location = e.getKey();
				LocationStat stat = e.getValue();

				if (first) {
					first = false;
				} else {
					sb.append(',');
					sb.append('\n');
				}

				sb.append('[');
				sb.append(location.getLat()).append(',').append(location.getLng()).append(',').append(stat.getCount());
				sb.append(']');
			}

			sb.append(']');

			return sb.toString();
		}
	}

	public static class LocationStat extends PojoWritable {
		public static final LocationStat ONCE = new LocationStat().add(1);

		private int m_count;

		public LocationStat add(int count) {
			m_count += count;
			return this;
		}

		public int getCount() {
			return m_count;
		}

		@Override
		public int hashCode() {
			return m_count;
		}
	}
}
