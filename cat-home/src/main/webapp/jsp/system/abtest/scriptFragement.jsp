<%@ page contentType="text/html; charset=utf-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model"
	scope="request" />
<%@ page import="java.util.List" %>
<%@ page import="com.dianping.cat.system.page.abtest.Model" %>
<%@ page import="com.dianping.cat.abtest.model.entity.*" %>

import java.util.Random; 
import javax.servlet.http.HttpServletRequest; 
import com.dianping.cat.system.abtest.conditions.ABTestCondition;
public class TrafficFilter {

<c:set var="count" value="1"></c:set>
<c:forEach var="case" items="${model.abtestModel.cases}">
	<c:forEach var="run" items="${case.runs}">
		<c:forEach var="condition" items="${run.conditions}">
			private Condition${count} m_condition${count} = new Condition${count}();
			<c:set var="count" value="${count + 1}"></c:set>
		</c:forEach>
	</c:forEach>
</c:forEach>

<c:set var="count1" value="1"></c:set>
<c:set var="isFirst1" value="1"></c:set>
<c:set var="isFirst2" value="1"></c:set>

public boolean isEligible(HttpServletRequest request) {
if(
<c:forEach var="case" items="${model.abtestModel.cases}">
	<c:forEach var="run" items="${case.runs}">
		<c:forEach var="condition" items="${run.conditions}">
			<c:if test="${isFirst1 == 0 }">
				<c:if test="${condition.seq == 4}">
					<c:if test="${isFirst2 == 0 }">
						)
					</c:if>
				</c:if>
				<c:if test="${operator == 0}">
              		&&
              		</c:if>
				<c:if test="${operator == 1}">
              		||
              		</c:if>
				<c:if test="${condition.seq == 3}">
					<c:if test="${isFirst2 == 1 }">
						(
						<c:set var="isFirst2" value="0"></c:set>
					</c:if>
				</c:if>
			</c:if>
			m_condition${count1}.accept(request)
			<c:if test="${condition.operator eq 'and' }">
           			<c:set var="operator" value="0"></c:set>
        		</c:if>
			<c:if test="${condition.operator eq 'or' }">
           			<c:set var="operator" value="1"></c:set>
           		</c:if>
           	<c:set var="isFirst1" value="0"></c:set>
           	
			<c:set var="count1" value="${count1 + 1}"></c:set>
		</c:forEach>
	</c:forEach>
</c:forEach>
) { return true; } return false;}

<%
	Model myVar = (Model)request.getAttribute("model");
	List<Condition> conditions = myVar.getAbtestModel().getCases().get(0).getRuns().get(0).getConditions();
	for(int i = 0 ; i < conditions.size(); i++){
		Condition condition = conditions.get(i);
		
		if(condition.getName().equals("url")){
			
%>
public class Condition<%=i+1%> implements ABTestCondition {
	@Override
	public boolean accept(HttpServletRequest request) {
		String actual = request.getRequestURL().toString();
		
		if (<%=myVar.getUrlScriptProvider().getFragement(condition) %>) {
			return true;
		} else {
			return false;
		}
	}

}	

<% }else if(condition.getName().equals("percent")){
%>
		
public class Condition<%=i+1%> implements ABTestCondition {
	private int m_percent = -1;

	private Random m_random = new Random();

	@Override
	public boolean accept(HttpServletRequest request) {
		if (m_percent == -1) {
			m_percent = <%=condition.getText() %>;
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
<%}}%>


}