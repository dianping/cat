package com.dianping.cat.consumer.problem.model;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultJsonBuilder;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.problem.model.transform.DefaultDomParser;
import com.site.helper.Files;

public class ProblemReportTest {
   @Test
   public void testXml() throws Exception {
      DefaultDomParser parser = new DefaultDomParser();
      String source = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report.xml"), "utf-8");
      ProblemReport root = parser.parse(source);
      String xml = new DefaultXmlBuilder().buildXml(root);
      String expected = source;

      Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
   }

   @Test
   public void testJson() throws Exception {
      DefaultDomParser parser = new DefaultDomParser();
      String source = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report.xml"), "utf-8");
      ProblemReport root = parser.parse(source);
      String json = new DefaultJsonBuilder().buildJson(root);
      String expected = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report.json"), "utf-8");

      Assert.assertEquals("XML is not well parsed or JSON is not well built!", expected.replace("\r", ""), json.replace("\r", ""));
   }

}
