//package com.dianping.cat.abtest.spi.internal;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import junit.framework.Assert;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.unidal.lookup.ComponentTestCase;
//import org.unidal.lookup.annotation.Inject;
//
//import com.dianping.cat.abtest.model.entity.Condition;
//import com.dianping.cat.abtest.spi.internal.conditions.ABTestCondition;
//import com.dianping.cat.abtest.spi.internal.conditions.ABTestConditionManager;
//import com.dianping.cat.abtest.spi.internal.conditions.AbstractABTestCondition;
//
//public class ABTestFilterManagerTest extends ComponentTestCase {
//	
//	private ABTestConditionManager m_manager;
//
//	@Before
//	public void prepare() throws Exception{
//	      m_manager = lookup(ABTestConditionManager.class);
//	}
//	@Test
//	public void testEmptyFilter() {
//		List<Condition> cons = new ArrayList<Condition>();
//		
//		Assert.assertEquals(true, m_manager.accept(cons,null));
//	}
//	
//	@Test
//	public void testNullFilter() {
//		
//		Assert.assertEquals(true, m_manager.accept(null,null));
//	}
//	
//	@Test
//	public void testSingleFilter() {
//		Condition con1 = getCondition(TestCondition.ID1,"and");
//
//		List<Condition> cons = new ArrayList<Condition>();
//		
//		cons.add(con1);
//		
//		Assert.assertEquals(false, m_manager.accept(cons,null));
//	}
//	
//	@Test
//	public void testMultipleFilter0() {
//		//true and false and false or true
//		Condition con1 = getCondition(TestCondition.ID0,"and");
//		Condition con2 = getCondition(TestCondition.ID1,"and");
//		Condition con3 = getCondition(TestCondition.ID1,"or");
//		Condition con4 = getCondition(TestCondition.ID0,"and");
//
//		List<Condition> cons = new ArrayList<Condition>();
//		
//		cons.add(con1);
//		cons.add(con2);
//		cons.add(con3);
//		cons.add(con4);
//		
//		Assert.assertEquals(true, m_manager.accept(cons,null));
//	}
//	
//	@Test
//	public void testMultipleFilter1() {
//		//true or false or true and false
//		Condition con1 = getCondition(TestCondition.ID0,"or"); 
//		Condition con2 = getCondition(TestCondition.ID1,"or");
//		Condition con3 = getCondition(TestCondition.ID0,"and");
//		Condition con4 = getCondition(TestCondition.ID1,"and");
//
//		List<Condition> cons = new ArrayList<Condition>();
//		
//		cons.add(con1);
//		cons.add(con2);
//		cons.add(con3);
//		cons.add(con4);
//		
//		Assert.assertEquals(true, m_manager.accept(cons,null));
//	}
//	
//	@Test
//	public void testMultipleFilter2() {
//		//true and false or false and true or true
//		Condition con1 = getCondition(TestCondition.ID0,"and");
//		Condition con2 = getCondition(TestCondition.ID1,"or");
//		Condition con3 = getCondition(TestCondition.ID1,"and");
//		Condition con4 = getCondition(TestCondition.ID0,"or");
//		Condition con5 = getCondition(TestCondition.ID0,"or");
//
//		List<Condition> cons = new ArrayList<Condition>();
//		
//		cons.add(con1);
//		cons.add(con2);
//		cons.add(con3);
//		cons.add(con4);
//		cons.add(con5);
//		
//		Assert.assertEquals(true, m_manager.accept(cons,null));
//	}
//	
//
//	public Condition getCondition(String name, String op){
//		Condition condition = new Condition();
//		
//		condition.setName(name);
//		condition.setOperator(op);
//		
//		return condition;
//	}
//	
//	public static class TestCondition extends AbstractABTestCondition implements ABTestCondition {
//		public static final String ID0 = "true";
//		public static final String ID1 = "false";
//		
//		@Inject
//		private boolean m_march;
//
//		@Override
//		public boolean accept(Condition condition) {
//			return m_march;
//		}
//
//		public boolean isMarch() {
//      	return m_march;
//      }
//
//		public void setMarch(boolean march) {
//      	m_march = march;
//      }
//	}
//
//}
