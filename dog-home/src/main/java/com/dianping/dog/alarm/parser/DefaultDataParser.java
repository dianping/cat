package com.dianping.dog.alarm.parser;

import com.dianping.dog.alarm.connector.RowData;

public class DefaultDataParser implements DataParser {

	@SuppressWarnings("hiding")
   @Override
   public <String> RowData parse(String context) {
		
	   return new RowData();
   }

}
