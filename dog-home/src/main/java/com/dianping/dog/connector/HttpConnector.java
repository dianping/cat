package com.dianping.dog.connector;

import java.net.URL;

import com.site.helper.Files;

public class HttpConnector extends AbstractConnector<String> {

	@Override
   public String fetchContent(ConnectorContext ctx) {
		String url = ctx.getUrl();
		try {
	      URL data = new URL(url);
	      String content = Files.forIO().readFrom(data.openStream(), "utf-8");
	      return content;
      } catch (Exception e) {
	      
      }
		return null;
   }

}
