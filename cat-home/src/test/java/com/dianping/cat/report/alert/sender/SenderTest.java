package com.dianping.cat.report.alert.sender;

import java.util.Arrays;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.alert.sender.sender.MailSender;
import com.dianping.cat.report.alert.sender.sender.Sender;
import com.dianping.cat.report.alert.sender.sender.SmsSender;
import com.dianping.cat.report.alert.sender.sender.WeixinSender;

public class SenderTest extends ComponentTestCase {

	@Test
	public void test() {
		Map<String, Sender> mailSender = lookupMap(Sender.class);
		String content = "[CAT 第三方告警] [项目: ] : [[type=get, details=HTTP URL[1234568888888888.com?] GET访问出现异常]][时间: 2015-01-15 18:20] \n<a href='http://cat/r/p?domain=&date=2015011518'>点击此处查看详情</a>";
		AlertMessageEntity entity = new AlertMessageEntity("CAT", "[CAT第三方告警] [项目: ]", AlertType.ThirdParty.getName(),
		      content, Arrays.asList("jialin.sun@dianping.com"));
		AlertMessageEntity entity2 = new AlertMessageEntity("CAT", "[CAT第三方告警] [项目: ]", AlertType.ThirdParty.getName(),
		      content, Arrays.asList("15201789489"));

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
