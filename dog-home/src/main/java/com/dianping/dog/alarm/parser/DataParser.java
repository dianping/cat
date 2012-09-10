package com.dianping.dog.alarm.parser;

import com.dianping.dog.alarm.connector.RowData;

public interface DataParser {
	 
	<T> RowData parse(T context);
    
} 
