package com.dianping.dog.alarm.parser;

import org.json.JSONObject;

import com.dianping.dog.alarm.connector.RowData;

public class DefaultDataParser implements DataParser {

	@SuppressWarnings("hiding")
   @Override
   public <String> RowData parse(String context) {
      JSONObject jsonObject = new JSONObject(context);  
	   return new RowData();
   }

}
