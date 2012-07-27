package com.dianping.cat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import com.site.helper.Files;

public class TestHttp {

	private Server server;
	
	@Before
	public void before() {
		try {
			server = new Server(2281);
			WebAppContext context = new WebAppContext();

			System.setProperty("devMode", "true");
			context.setContextPath("/cat");
			context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
			context.setResourceBase("src/main/webapp");
			server.setHandler(context);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void after(){
		try {
	      server.stop();
      } catch (Exception e) {
	      e.printStackTrace();
      }
	}
	
	private String getContentByUrl(String urlAddress) {
		try {
			URL url = new URL(urlAddress);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			int nRc = http.getResponseCode();

			if (nRc == HttpURLConnection.HTTP_OK) {
				InputStream in = http.getInputStream();
				return Files.forIO().readFrom(in, "utf-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Test
	public void testHome() {
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/home") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/home?op=checkpoint") != null);
	}
	
	@Test
	public void testTransaction(){
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/t") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/t?domain=Cat&ip=All&type=URL") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/t?op=graphs&domain=Cat&date=2012072623&ip=All&type=URL") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/t?op=history") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/t?op=history&domain=Cat&ip=All&type=URL") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/t?op=historyGraph&domain=Cat&date=2012072623&ip=All&type=URL") != null);
	}
	
	@Test
	public void testEvent(){
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/e") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/e?domain=Cat&ip=All&type=URL") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/e?op=graphs&domain=Cat&date=2012072623&ip=All&type=URL") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/e?op=history") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/e?op=history&domain=Cat&ip=All&type=URL") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/e?op=historyGraph&domain=Cat&date=2012072623&ip=All&type=URL") != null);
	}
	
	@Test
	public void testProblem(){
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/p") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/p?op=history") != null);
	}
	
	@Test
	public void testHeartbeat(){
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/h") != null);
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/h?op=history") != null);
	
	}
	
	@Test
	public void testMatrix(){
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/matrix") != null);
	}
	
	@Test
	public void testTask(){
		Assert.assertEquals(true, getContentByUrl("http://localhost:2281/cat/r/task") != null);
	}
}
