package com.dianping.dog.parser;

import com.dianping.dog.connector.RowData;

public class DefaultDataParser implements DataParser {

	@SuppressWarnings("hiding")
   @Override
   public <String> RowData parse(String context) {
	   return new RowData();
   }

}
