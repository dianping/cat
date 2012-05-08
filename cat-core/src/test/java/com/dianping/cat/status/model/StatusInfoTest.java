package com.dianping.cat.status.model;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.transform.DefaultXmlBuilder;
import com.dianping.cat.status.model.transform.DefaultDomParser;
import com.site.helper.Files;

public class StatusInfoTest {
   @Test
   public void testXml() throws Exception {
      DefaultDomParser parser = new DefaultDomParser();
      String source = Files.forIO().readFrom(getClass().getResourceAsStream("status.xml"), "utf-8");
      StatusInfo root = parser.parse(source);
      String xml = new DefaultXmlBuilder().buildXml(root);
      String expected = source;

      Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
   }
}
