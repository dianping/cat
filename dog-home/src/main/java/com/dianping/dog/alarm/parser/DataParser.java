package com.dianping.dog.alarm.parser;

import org.codehaus.plexus.logging.LogEnabled;

import com.dianping.dog.alarm.connector.RowData;
import com.dianping.dog.alarm.entity.ConnectEntity;

public interface DataParser extends LogEnabled{
	 
	<T> RowData parse(ConnectEntity conEntity,T context);
	
	RowData mergeRowData(RowData newData,RowData oldData);
    
} 
