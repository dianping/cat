package com.dianping.dog.alarm.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.dianping.dog.alarm.connector.RowData;

public class DefaultDataParser implements DataParser {

	@SuppressWarnings("hiding")
   @Override
   public <String> RowData parse(String context) {
		RowData data = new RowData();
      try {
      	JSONObject jsonObject = new JSONObject(context.toString().trim());  
	      long timeStamp = Long.parseLong(jsonObject.get("timestamp").toString());
	      long totalCount = Long.parseLong(jsonObject.get("Count").toString());
	      data.addData("time", timeStamp);
	      data.addData("totalCount",totalCount);
      } catch (JSONException e) {
	      e.printStackTrace();
      }
	   return new RowData();
   }

}
