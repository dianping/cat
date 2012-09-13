package com.dianping.dog.alarm.parser;

import com.dianping.dog.alarm.entity.ConnectEntity;

public class DataParserFactory {
	
	public DataParser getDataParser(ConnectEntity conEntity){
		if(conEntity.getReport() == "problem"){
			return new ProblemDataParser();
		}
   	return null;
   }
    
}
