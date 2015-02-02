package com.dianping.cat;

import java.text.MessageFormat;
import java.text.ParseException;

import org.junit.Test;

public class MessageFomatTest {

	@Test
	public void test(){
		String str="/topic/s_c_2_0_r0123123123/123123123";
		MessageFormat format = new MessageFormat("/topic/{0}");
		
		try {
	      format.parse(str);
      } catch (ParseException e) {
	      e.printStackTrace();
      }
		
	}
}
