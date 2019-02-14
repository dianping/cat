/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.alert.sender;

import java.util.Arrays;
import java.util.Map;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.sender.MailSender;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.Sender;
import com.dianping.cat.alarm.spi.sender.SmsSender;
import com.dianping.cat.alarm.spi.sender.WeixinSender;

public class SenderTest extends ComponentTestCase {

	@Test
	public void test() {
		Map<String, Sender> mailSender = lookupMap(Sender.class);
		String content = "[CAT 第三方告警] [项目: ] : [[type=get, details=HTTP URL[1234568888888888.com?] GET访问出现异常]][时间: 2015-01-15 18:20] \n<a href='http://cat/r/p?domain=&date=2015011518'>点击此处查看详情</a>";
		SendMessageEntity entity = new SendMessageEntity("CAT", "[CAT第三方告警] [项目: ]", AlertType.Transaction.getName(),	content,
								Arrays.asList("jialin.sun@dianping.com"));
		SendMessageEntity entity2 = new SendMessageEntity("CAT", "[CAT第三方告警] [项目: ]", AlertType.Transaction.getName(),	content,
								Arrays.asList("15201789489"));

		Assert.assertEquals(true, mailSender.get(MailSender.ID).send(entity));
		Assert.assertEquals(true, mailSender.get(WeixinSender.ID).send(entity));
		Assert.assertEquals(true, mailSender.get(SmsSender.ID).send(entity2));
	}

	//    @Test
	//    public void testJson() {
	//        SubItem item = new SubItem();
	//        item.setTest("subTest");
	//
	//        Item i = new Item();
	//        i.setTest("test");
	//        i.setItem(item);
	//
	//        JsonBuilder jsonBuilder = new JsonBuilder();
	//        String json = jsonBuilder.toJson(i);
	//        System.out.println(json);
	//        Item result = (Item) jsonBuilder.parse(json, Item.class);
	//        System.out.println(jsonBuilder.toJson(result));
	//    }

	public static class Item {
		private String test;

		private SubItem item;

		public String getTest() {
			return test;
		}

		public void setTest(String test) {
			this.test = test;
		}

		public SubItem getItem() {
			return item;
		}

		public void setItem(SubItem item) {
			this.item = item;
		}

	}

	public static class SubItem {
		private String test;

		public String getTest() {
			return test;
		}

		public void setTest(String test) {
			this.test = test;
		}
	}
}
