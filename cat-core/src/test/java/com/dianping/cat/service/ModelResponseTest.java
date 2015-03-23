package com.dianping.cat.service;

import org.junit.Test;

import com.dianping.cat.report.service.ModelResponse;

import junit.framework.Assert;

public class ModelResponseTest {

	@Test
	public void test(){
		ModelResponse<String> response = new ModelResponse<String>();
		String model = "model";
		NullPointerException exception = new NullPointerException();
		
		response.setException(exception);
		response.setModel(model);
		
		Assert.assertEquals(model, response.getModel());
		Assert.assertEquals(exception,response.getException());
		Assert.assertEquals("ModelResponse[model=model, exception=java.lang.NullPointerException]", response.toString());
	}
}
