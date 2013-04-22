package com.dianping.cat.report.analyzer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.state.StateShow;
import com.dianping.cat.report.service.ReportService;

@RunWith(JUnit4.class)
public class CatStateAnalyzer extends ComponentTestCase {

	@Inject
	private ReportService m_reportService;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy/MM/dd");

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_reportService = lookup(ReportService.class);
	}

	@Test
	public void test() throws ParseException {
		StringBuilder sb = new StringBuilder();

		sb.append("Date").append('\t').append("total").append('\t').append("m_loss").append('\t');
		sb.append("avgNumber").append('\t').append("dumpSize").append('\n');

		System.out.println(sb);
		String startDate = "2012/11/01";

		Date start = m_sdf.parse(startDate);
		for(long i=start.getTime();i<System.currentTimeMillis();i=i+TimeUtil.ONE_DAY){
			State state = buildState(new Date(i));
			
			System.out.println(state);
		}
	}

	private State buildState(Date date) {
		StateReport report = m_reportService.queryStateReport("Cat", date, new Date(date.getTime() + TimeUtil.ONE_DAY));
		State state = new State(m_sdf.format(date));

		if (report != null) {
			StateShow show = new StateShow("All");
			show.visitStateReport(report);

			state.setTotal(show.getTotal().getTotal());
			state.setLoss(show.getTotal().getTotalLoss());
			double avgNumber = ((double) show.getTotal().getTotal() / 1440.0);
			state.setAvgNumber((long) avgNumber);
			state.setDumpSize((long)(show.getTotal().getSize() / 1024.0/ 1024 / 1024));
		}
		return state;
	}

	public static class State {
		private String m_date;

		private long m_total;

		private long m_avgNumber;

		private long m_loss;

		private long m_dumpSize;

		public State(String date) {
			m_date = date;
		}

		public long getTotal() {
			return m_total;
		}

		public void setTotal(long total) {
			m_total = total;
		}

		public long getLoss() {
			return m_loss;
		}

		public void setLoss(long loss) {
			m_loss = loss;
		}

		public String getDate() {
			return m_date;
		}

		public void setDate(String date) {
			m_date = date;
		}

		public long getAvgNumber() {
			return m_avgNumber;
		}

		public void setAvgNumber(long avgNumber) {
			m_avgNumber = avgNumber;
		}

		public long getDumpSize() {
			return m_dumpSize;
		}

		public void setDumpSize(long dumpSize) {
			m_dumpSize = dumpSize;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append(m_date).append('\t').append(m_total).append('\t').append(m_loss).append('\t');
			sb.append(m_avgNumber).append('\t').append(m_dumpSize);
			return sb.toString();
		}
	}

}
