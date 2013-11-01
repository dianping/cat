package com.dianping.cat.consumer.sql;

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
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class SqlAnalyzerTest extends ComponentTestCase {

	private long m_timestamp;

	private SqlAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai")); 
		long currentTimeMillis = System.currentTimeMillis();

		m_timestamp = currentTimeMillis - currentTimeMillis % (3600 * 1000);

		m_analyzer = (SqlAnalyzer) lookup(MessageAnalyzer.class, SqlAnalyzer.ID);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 100; i++) {
			MessageTree tree = generateMessageTree(i);

			m_analyzer.process(tree);
		}

		SqlReport report = m_analyzer.getReport(m_domain);

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("sql_analyzer.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t = new DefaultTransaction("SQL", "", null);
		
		Event eventBase = new DefaultEvent("SQL.Database", "mysql://10.1.1.220:3306/cat");
		
		t.addChild(eventBase);
		
		if(i%4 == 1 ){
			Event event = new DefaultEvent("SQL.Method", "SELECT");
			
			t.addChild(event);
			t.addData("select * from cat");
		}else if(i%4 == 2 ){
			Event event = new DefaultEvent("SQL.Method", "DELETE");
			
			t.addChild(event);
			t.addData("delete from cat where id = 1");
		}else if(i%4 == 3 ){
			Event event = new DefaultEvent("SQL.Method", "INSERT");
			
			t.addChild(event);
			t.addData("insert into cat2 (a,b) values (1,1)");
		}else if(i%4 == 0 ){
			Event event = new DefaultEvent("SQL.Method", "UPDATE");
			
			t.addChild(event);
			t.addData("update cat2 set a = 1 where id = 1");
		}


		t.complete();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + 1000);
		tree.setMessage(t);

		return tree;
	}

}
