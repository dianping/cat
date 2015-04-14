<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.statistics.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.statistics.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.statistics.Model" scope="request"/>


<table class="table table-striped table-condensed   table-hover" id="contents" width="100%">
	<thead>
		<tr >
			<th width="60%">异常</th>
			<th width="20%">Warning警告</th>
			<th width="20%">Error警告</th>
		</tr>
	</thead>
	
	<tbody>
	<c:forEach var="exception" items="${model.alertExceptions}">	
		<tr>
			<td>${exception.id}</td>
			<td>${exception.warnNumber}</td>
			<td>${exception.errorNumber}</td>
		</tr>
	</c:forEach>
	</tbody>
</table>