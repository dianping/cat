package com.dianping.dog.alarm.parser;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.dog.alarm.entity.ConnectEntity;

public class DataParserFactory implements LogEnabled {
	
	private Logger m_logger;
	
	public DataParser getDataParser(ConnectEntity conEntity){
		//TODO need to fix
		if(conEntity.getReport().equals("problem")){
			DataParser parser =  new ProblemDataParser();
			parser.enableLogging(this.m_logger);
			return parser;
		}
   	return null;
   }

	@Override
   public void enableLogging(Logger logger) {
		this.m_logger = logger;	   
   }
    
}
