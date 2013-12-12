package com.dianping.cat.abtest.groupstrategy;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.system.page.abtest.util.GsonManager;
import com.google.gson.Gson;

public class GsonBuilderManagerTest {
	
	@Test
	public void test_none_prefix_field(){
		GsonManager manager = new GsonManager();
		Gson gson = manager.getGson();
		
		MockObject object = new MockObject("name", "type");
		String json = gson.toJson(object, MockObject.class);

		Assert.assertEquals("{\"name\":\"name\",\"type\":\"type\"}", json);
	}

	public class MockObject {
		private String m_name;
		private String m_type;

		public MockObject(String name, String type) {
			m_name = name;
			m_type = type;
		}

		@Override
		public String toString() {
			return "MockObject [m_name=" + m_name + ", m_type=" + m_type + "]";
		}
	}
}
