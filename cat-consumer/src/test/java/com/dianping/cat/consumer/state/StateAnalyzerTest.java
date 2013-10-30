package com.dianping.cat.consumer.state;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.StateReport;

public class StateAnalyzerTest extends ComponentTestCase {

	private StateAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		Date date = sdf.parse("20120101 00:00:00");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(Calendar.MILLISECOND, 0);

		m_analyzer = (StateAnalyzer) lookup(MessageAnalyzer.class, StateAnalyzer.ID);
		m_analyzer.initialize(calendar.getTime().getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {

		StateReport report = m_analyzer.getReport(m_domain);
		
		for(Machine machine : report.getMachines().values()){
			for(Message message : machine.getMessages().values()){
				System.out.println(message);
			}
		}
		
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("state_analyzer.xml"), "utf-8");
		
		System.out.println(expected);
		
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}
	
	public static void main(String[] args) throws ParseException {
   }
}
