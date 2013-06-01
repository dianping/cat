package com.dianping.cat.report.page.dependency.graph;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.tuple.Pair;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;

public class GraphConfigManagerTest {
	private TopologyGraphConfigManger m_manager;

	private File m_file;

	private String m_orginalContent;

	@After
	public void after() throws IOException {
		Files.forIO().writeTo(m_file, m_orginalContent);
	}

	@Before
	public void setUp() throws InitializationException, IOException {
		m_manager = new TopologyGraphConfigManger();
		m_file = new File("src/test/resources/com/dianping/cat/report/page/dependency/graph/ConfigForTest.xml");
		m_orginalContent = Files.forIO().readFrom(m_file, "utf-8");
		m_manager.setFileName(m_file.getAbsolutePath());
		m_manager.initialize();
	}

	@Test
	public void testAddModifyDeleteEdge() throws IOException {
		String type = "PigeonCall";
		String from = "TuanGouWeb";
		String to = "TuanGouService";
		EdgeConfig config = new EdgeConfig(type + ":" + from + ":" + to);

		config.setType(type);
		config.setFrom(from);
		config.setTo(to);
		config.setErrorResponseTime(1.1);
		config.setErrorThreshold(1);
		config.setWarningResponseTime(2.2);
		config.setWarningThreshold(2);

		m_manager.insertEdgeConfig(config);

		String addContent = Files.forIO().readFrom(GraphConfigManagerTest.class.getResourceAsStream("AddEdgeConfig.xml"),
		      "utf-8");
		Assert.assertEquals(addContent.replaceAll("\r", ""), m_manager.getConfig().toString().replaceAll("\r", ""));

		config = new EdgeConfig(from);
		config.setType(type);
		config.setFrom(from);
		config.setTo(to);
		config.setErrorResponseTime(11.1);
		config.setErrorThreshold(11);
		config.setWarningResponseTime(22.2);
		config.setWarningThreshold(22);
		m_manager.insertEdgeConfig(config);

		String updateContent = Files.forIO().readFrom(
		      GraphConfigManagerTest.class.getResourceAsStream("UpdateEdgeConfig.xml"), "utf-8");
		Assert.assertEquals(updateContent.replaceAll("\r", ""), m_manager.getConfig().toString().replaceAll("\r", ""));

		m_manager.deleteEdgeConfig(type, from, to);
		Assert.assertEquals(m_orginalContent.replaceAll("\r", ""), m_manager.getConfig().toString().replaceAll("\r", ""));
	}

	@Test
	public void testAddModifyDeleteNode() throws IOException {
		String type = "URL";
		String domain = "Cat";
		DomainConfig config = new DomainConfig(domain);
		config.setErrorResponseTime(1.1);
		config.setErrorThreshold(1);
		config.setWarningResponseTime(2.2);
		config.setWarningThreshold(2);
		m_manager.insertDomainConfig(type, config);

		String addContent = Files.forIO().readFrom(
		      GraphConfigManagerTest.class.getResourceAsStream("AddDomainConfig.xml"), "utf-8");
		Assert.assertEquals(addContent.replaceAll("\r", ""), m_manager.getConfig().toString().replaceAll("\r", ""));

		config = new DomainConfig(domain);
		config.setErrorResponseTime(11.1);
		config.setErrorThreshold(11);
		config.setWarningResponseTime(22.2);
		config.setWarningThreshold(22);
		m_manager.insertDomainConfig(type, config);

		String updateContent = Files.forIO().readFrom(
		      GraphConfigManagerTest.class.getResourceAsStream("UpdateDomainConfig.xml"), "utf-8");
		Assert.assertEquals(updateContent.replaceAll("\r", ""), m_manager.getConfig().toString().replaceAll("\r", ""));

		m_manager.deleteDomainConfig(type, domain);
		Assert.assertEquals(m_orginalContent.replaceAll("\r", ""), m_manager.getConfig().toString().replaceAll("\r", ""));
	}
	
	@Test
	public void testBuildNodeStateByError(){
		Index index = new Index("URL");
		index.setAvg(40.0);
		index.setErrorCount(50);
		index.setSum(1000.0);
		index.setTotalCount(100);
		
		Pair<Integer, String> state = m_manager.buildNodeState("Cat", index);
		Assert.assertEquals(state, null);
		
		index.setErrorCount(100);
		state = m_manager.buildNodeState("Cat", index);
		Assert.assertEquals(TopologyGraphItemBuilder.WARN, (int)state.getKey());
		Assert.assertEquals("ERROR:100\n", state.getValue());

		index.setErrorCount(200);
		state = m_manager.buildNodeState("Cat", index);
		Assert.assertEquals(TopologyGraphItemBuilder.ERROR, (int)state.getKey());
		Assert.assertEquals("ERROR:200\n", state.getValue());
	}
	@Test
	public void testBuildNodeStateByResponse(){
		Index index = new Index("URL");
		String domain = "UserWeb";
		index.setAvg(5.0);
		index.setErrorCount(5);
		index.setSum(1000.0);
		index.setTotalCount(100);
		
		Pair<Integer, String> state = m_manager.buildNodeState(domain, index);
		Assert.assertEquals(state, null);
		
		index.setAvg(10.0);
		state = m_manager.buildNodeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.WARN, (int)state.getKey());
		Assert.assertEquals("AVG:10.0\n", state.getValue());

		index.setAvg(100.0);
		state = m_manager.buildNodeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.ERROR, (int)state.getKey());
		Assert.assertEquals("AVG:100.0\n", state.getValue());
	}
	
	@Test
	public void testBuildEdgeStateByDefault(){
		Dependency index = new Dependency();
		String service = "TuanGouService";
		index.setType("PigeonCall");
		String domain = "Cat";
		index.setTarget(service);
		index.setAvg(40.0);
		index.setErrorCount(50);
		index.setSum(1000.0);
		index.setTotalCount(100);
		
		Pair<Integer, String> state = m_manager.buildEdgeState(domain, index);
		Assert.assertNull(state);
		
		index.setErrorCount(100);
		state = m_manager.buildEdgeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.WARN, (int)state.getKey());
		Assert.assertEquals("ERROR:100\n", state.getValue());

		index.setErrorCount(200);
		state = m_manager.buildEdgeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.ERROR, (int)state.getKey());
		Assert.assertEquals("ERROR:200\n", state.getValue());
	}
	

	@Test
	public void testBuildEdgeStateByConfig(){
		Dependency index = new Dependency();
		String domain = "Cat";
		String service = "UserService";
		index.setType("PigeonCall");
		index.setTarget(service);
		index.setAvg(9.0);
		index.setErrorCount(9);
		index.setSum(1000.0);
		index.setTotalCount(100);
		
		Pair<Integer, String> state = m_manager.buildEdgeState(domain, index);
		Assert.assertNull(state);
		
		index.setAvg(10.0);
		state = m_manager.buildEdgeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.WARN, (int)state.getKey());
		Assert.assertEquals("AVG:10.0\n", state.getValue());

		index.setAvg(100.0);
		state = m_manager.buildEdgeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.ERROR, (int)state.getKey());
		Assert.assertEquals("AVG:100.0\n", state.getValue());
	}
	
	@Test
	public void testBuildEdgeStateByProjectConfig(){
		Dependency index = new Dependency();
		String domain = "UserWeb";
		String service = "UserService";
		index.setType("PigeonCall");
		index.setTarget(service);
		index.setAvg(9.0);
		index.setErrorCount(9);
		index.setSum(1000.0);
		index.setTotalCount(100);
		
		Pair<Integer, String> state = m_manager.buildEdgeState(domain, index);
		Assert.assertNull(state);
		
		index.setAvg(10.0);
		state = m_manager.buildEdgeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.WARN, (int)state.getKey());
		Assert.assertEquals("AVG:10.0\n", state.getValue());

		index.setAvg(20.0);
		state = m_manager.buildEdgeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.ERROR, (int)state.getKey());
		Assert.assertEquals("AVG:20.0\n", state.getValue());
		
		index.setAvg(9.0);
		
		
		index.setErrorCount(10);
		state = m_manager.buildEdgeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.WARN, (int)state.getKey());
		Assert.assertEquals("ERROR:10\n", state.getValue());

		index.setErrorCount(20);
		state = m_manager.buildEdgeState(domain, index);
		Assert.assertEquals(TopologyGraphItemBuilder.ERROR, (int)state.getKey());
		Assert.assertEquals("ERROR:20\n", state.getValue());
	}

}
