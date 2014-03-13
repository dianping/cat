package com.dianping.cat.consumer.state;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;

public class StateAnalyzerTest extends ComponentTestCase {

	private StateAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));  
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss:SS");
		Date date = sdf.parse("20120101 00:00:00:00");

		m_analyzer = (StateAnalyzer) lookup(MessageAnalyzer.class, StateAnalyzer.ID);
		
		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		StateReport report = m_analyzer.getReport(m_domain);
		
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("state_analyzer.xml"), "utf-8");
		
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}
}
