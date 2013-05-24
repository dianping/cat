package com.dianping.cat.abtest.demo.annotation;

import com.dianping.cat.abtest.annotation.Cookie;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;


public class CookieGroupStrategy implements ABTestGroupStrategy{

	@Cookie("cityId")
	private String cityId;
	
	@Override
   public void apply(ABTestContext ctx) {
	   if(cityId.equals("default")){
	   }else{
	   	ctx.setGroupName("A");
	   }
   }
	
	

}
