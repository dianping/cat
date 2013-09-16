package com.dianping.cat.abtest.conditions;

import java.util.Random; 
import javax.servlet.http.HttpServletRequest; 
import com.dianping.cat.abtest.spi.internal.conditions.ABTestCondition;

public class TrafficFilter {

<#assign count = 1> 
<#list run.conditions as condition>
	private Condition${count} m_condition${count} = new Condition${count}();

	<#assign count = count + 1> 
</#list>

	<#assign count1 = 1> 
	<#assign isFirst1 = 1> 
	<#assign isFirst2 = 1> 
	public boolean isEligible(HttpServletRequest request) {
		if(
		<#list run.conditions as condition>
			<#if isFirst1 = 0>
				<#if condition.seq = 4>
					<#if isFirst2 = 0>
						)
					</#if>
				</#if>
				<#if operator = 0>
					&&
				</#if> 
				<#if operator = 1>
					||
				</#if>
				<#if condition.seq = 3>
					<#if isFirst2 = 1>
						(
						<#assign isFirst2 = 0>
					</#if> 
				</#if>
			</#if>
			m_condition${count1}.accept(request)		
			<#if condition.operator = "and">
				<#assign operator = 0>
			</#if>
			<#if condition.operator = "or">
				<#assign operator = 1>
			</#if>
			
			<#assign isFirst1 = 0>
			<#assign count1 = count1 + 1> 
		</#list>
		) { 
			return true; 
		} 
		
		return false;
	}
	
	<#assign count1 = 1> 
	<#list run.conditions as condition>
		<#if condition.name = "url">
	public class Condition${count1} implements ABTestCondition {
		@Override
		public boolean accept(HttpServletRequest request) {
			String actual = request.getRequestURL().toString();
			
			if (${urlScriptProvider.getFragement(condition)}) {
				return true;
			} else {
				return false;
			}
		}
	}	
		</#if>
		
		<#if condition.name = "percent">
	public class Condition${count1} implements ABTestCondition {
		private int m_percent = -1;
	
		private Random m_random = new Random();
	
		@Override
		public boolean accept(HttpServletRequest request) {
			if (m_percent == -1) {
				m_percent = ${condition.text};
			}
	
			if (m_percent == 100) {
				return true;
			}
	
			int random = m_random.nextInt(100) + 1;
	
			if (random <= m_percent) {
				return true;
			} else {
				return false;
			}
		}
	}
		</#if>
		
		<#assign count1 = count1 + 1> 
	</#list>
}