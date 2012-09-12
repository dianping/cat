package com.dianping.dog.alarm.connector;

import java.net.URL;

import com.dianping.dog.alarm.entity.ConnectEntity;
import com.site.helper.Files;

public class HttpConnector extends AbstractConnector<String> {

	@Override
   public String fetchContent(ConnectEntity entity) {
		String url = entity.getUrl();
		try {
	      URL data = new URL(url);
	      String content = Files.forIO().readFrom(data.openStream(), "utf-8");
	      return content;
      } catch (Exception e) {
	      
      }
		return null;
   }
}
