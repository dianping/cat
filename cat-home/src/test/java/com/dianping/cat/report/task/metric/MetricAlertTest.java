package com.dianping.cat.report.task.metric;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;
import org.xml.sax.SAXException;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class MetricAlertTest extends ComponentTestCase {
	@Test
	public void testMetricAlert() {
		try {
			MetricAlert alert = lookup(MetricAlert.class);
			ModelService<MetricReport> modelService = new MyModelService();
			alert.m_service = modelService;
			SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			alert.metricAlert(format.parse("2013-07-10 10:00:00"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class MyModelService implements ModelService<MetricReport> {

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
      public ModelResponse<MetricReport> invoke(ModelRequest request) {
	      ModelResponse<MetricReport> response= new ModelResponse<MetricReport>();
	      String xml;
         try {
	         xml = Files.forIO().readFrom(getClass().getResourceAsStream("metricReport.xml"),
	               "utf-8");
		      MetricReport report = DefaultSaxParser.parse(xml);
		      response.setModel(report);
         } catch (IOException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
         } catch (SAXException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
         }
	      return response;
      }

		@Override
		public boolean isEligable(ModelRequest request) {
			return true;
		}

	}
}
