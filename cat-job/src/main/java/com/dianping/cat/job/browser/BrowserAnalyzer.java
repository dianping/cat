package com.dianping.cat.job.browser;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.dianping.cat.hadoop.mapreduce.MessageTreeWritable;
import com.dianping.cat.job.DefaultPartitioner;
import com.dianping.cat.job.JobFactory;
import com.dianping.cat.job.JobMeta;
import com.dianping.cat.job.MapperMeta;
import com.dianping.cat.job.PojoWritable;
import com.dianping.cat.job.ReducerMeta;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

@JobMeta(name = "browser", mapper = BrowserAnalyzer.BrowserMapper.class, reducer = BrowserAnalyzer.BrowserReducer.class, partitioner = DefaultPartitioner.class, reducerNum = 3)
public class BrowserAnalyzer extends Configured implements Tool {
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new BrowserAnalyzer(), args);

		System.exit(exitCode);
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = JobFactory.INSTANCE.createJob(this, getConf(), args);

		if (job.waitForCompletion(true)) {
			return 0;
		} else {
			return -1;
		}
	}

	/**
	 * <pre>
	 * MApi 1.0 (dpscope 4.8.1 appstore; iPhone 5.1)
	 * 
	 * MApi 1.0 (dpscopehd 1.5.1 appstore; iPad 5.1)
	 * 
	 * MApi 1.0 (dpscope 4.9 appstore; iPhone 4.2.1)
	 * 
	 * MApi 1.0 (dpscope 4.6.1 appstore; iPhone 4.2.1)
	 * 
	 * MApi 1.0 (dpscope 4.8.1 ppzhushou; iPhone 5.0.1)
	 * 
	 * MApi 1.0 (dpscope 4.8.1 appstore; iPhone 5.0.1)
	 * 
	 * MApi 1.0 (dpscope 4.7 appstore; iPhone 5.0.1)
	 * 
	 * MApi 1.0 (com.dianping.v1 4.7.1 dianping Lenovo_A60; Android 2.3.3)
	 * 
	 * MApi 1.0 (com.dianping.v1 4.9.1.193 null SCH-I519; Android 2.3.6)
	 * 
	 * MApi 1.0 (com.dianping.v1 4.7.1 91zs500 HTC_Desire_S; Android 2.3.5)
	 * </pre>
	 */
	public static class Browser extends PojoWritable {
		private static MessageFormat s_iosFormat = new MessageFormat("{0} ({5} {1} {4}; {2} {3})");

		private static MessageFormat s_androidFormat = new MessageFormat("{0} ({5} {1} {4} {6}; {2} {3})");

		private String m_name;

		private String m_version;

		private String m_osType;

		private String m_osVersion;

		private String m_channel;

		private String m_scope;

		private String m_deviceType;

		public Browser() {
		}
		
		public Browser(String userAgent) {
			init(userAgent);
		}

		public String getChannel() {
			return m_channel;
		}

		public String getDeviceType() {
			return m_deviceType;
		}

		public String getName() {
			return m_name;
		}

		public String getOsType() {
			return m_osType;
		}

		public String getOsVersion() {
			return m_osVersion;
		}

		public String getScope() {
			return m_scope;
		}

		public String getVersion() {
			return m_version;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 0;

			result = prime * result + ((m_channel == null) ? 0 : m_channel.hashCode());
			result = prime * result + ((m_deviceType == null) ? 0 : m_deviceType.hashCode());
			result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
			result = prime * result + ((m_osType == null) ? 0 : m_osType.hashCode());
			result = prime * result + ((m_osVersion == null) ? 0 : m_osVersion.hashCode());
			result = prime * result + ((m_scope == null) ? 0 : m_scope.hashCode());
			result = prime * result + ((m_version == null) ? 0 : m_version.hashCode());

			return result;
		}

		private void init(String userAgent) {
			Object[] values = null;

			try {
				if (values == null) {
					values = s_iosFormat.parse(userAgent);
				}
			} catch (ParseException e) {
				// ignore it
			}

			try {
				if (values == null) {
					values = s_androidFormat.parse(userAgent);
				}
			} catch (ParseException e) {
				// ignore it
			}

			if (values == null) {
				m_name = userAgent;
			} else {
				int len = values.length;
				int index = 0;

				m_name = len > index ? (String) values[index++] : null;
				m_version = len > index ? (String) values[index++] : null;
				m_osType = len > index ? (String) values[index++] : null;
				m_osVersion = len > index ? (String) values[index++] : null;
				m_channel = len > index ? (String) values[index++] : null;
				m_scope = len > index ? (String) values[index++] : null;
				m_deviceType = len > index ? (String) values[index++] : null;
			}
		}
	}

	@MapperMeta(keyOut = Browser.class, valueOut = BrowserStatatisics.class)
	public static class BrowserMapper extends Mapper<Object, MessageTreeWritable, Browser, BrowserStatatisics> {
		private static final String TOKEN = "&Agent=";

		private int m_total;

		private String getUserAgent(Transaction root) {
			List<Message> children = root.getChildren();

			for (Message child : children) {
				if (child instanceof Event && child.getType().equals("URL") && child.getName().equals("ClientInfo")) {
					// URL:ClientInfo &Agent=<ua>
					String data = child.getData().toString();
					int off = data.indexOf(TOKEN);

					if (off >= 0) {
						return data.substring(off + TOKEN.length());
					}

					break;
				}
			}

			return null;
		}

		@Override
		protected void map(Object key, MessageTreeWritable value, BrowserMapper.Context context) throws IOException,
		      InterruptedException {
			MessageTree tree = value.get();
			Message root = tree.getMessage();

			if (root instanceof Transaction && root.getName().equals("URL")) {
				String userAgent = getUserAgent((Transaction) root);

				if (userAgent != null) {
					Browser browser = new Browser(userAgent);

					context.write(browser, BrowserStatatisics.ONCE);
					m_total++;

					if (m_total % 1000 == 0) {
						context.setStatus(m_total + " processed");
						context.progress();
					}
				}
			}
		}
	}

	@ReducerMeta(keyIn = Browser.class, valueIn = BrowserStatatisics.class, keyOut = Browser.class, valueOut = BrowserStatatisics.class)
	public static class BrowserReducer extends Reducer<Browser, BrowserStatatisics, Browser, BrowserStatatisics> {
		protected void reduce(Browser browser, Iterable<BrowserStatatisics> stats, BrowserMapper.Context context)
		      throws IOException, InterruptedException {
			BrowserStatatisics all = new BrowserStatatisics();

			for (BrowserStatatisics stat : stats) {
				all.add(stat.getCount());
			}

			context.write(browser, all);
		}
	}

	public static class BrowserStatatisics extends PojoWritable {
		public static final BrowserStatatisics ONCE = new BrowserStatatisics().add(1);

		private int m_count;

		public BrowserStatatisics add(int count) {
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
