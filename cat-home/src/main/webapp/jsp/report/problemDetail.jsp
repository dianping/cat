<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.problem.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.problem.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.problem.Model" scope="request"/>
<table>
   <tr>
	  <th>Statistics</th>
   	  <c:forEach var="item" items="${model.statistics}">
   	  	<td>${item.type}  ${item.count}</td>
   	  </c:forEach>
   </tr>
</table>

<table >
   <tr>
	  <th>Error</th>
   	  <c:forEach var="entry" items="${model.entries}">
   	  	<td>${entry.messageId}</td>
   	  </c:forEach>
   </tr>
</table>