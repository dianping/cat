package com.dianping.dog.parser;

import com.dianping.dog.connector.RowData;

public interface DataParser {
	 
	<T> RowData parse(T context);
    
} 
