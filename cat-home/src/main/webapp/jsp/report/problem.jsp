<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.problem.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.problem.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.problem.Model" scope="request"/>
<c:set var="report" value="${model.report}"/>
<c:set var="threads" value="${report.machines[model.ipAddress].threads}"/>

<res:useCss value="${res.css.local.problem_css}" target="head-css"/>

<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<res:useJs value="${res.js.local.problem_js}" target="head-js"/>

<a:report title="Problem Report" navUrlPrefix="domain=${model.domain}&ip=${model.ipAddress}" timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

<jsp:body>

<table class="machines">
   <tr>
	  <th>Machines:</th>
   	  <c:forEach var="machine" items="${report.machines}">
   	  	<td><a href="?domain=${model.domain}&ip=${model.ipAddress}">${machine.value.ip}</a></td>
   	  </c:forEach>
   </tr>
</table>

<table class="problem">
	<tr>
		<th>Time</th>
		<c:forEach var="thread" items="${threads}">
			<th>${thread.value.id}</th>
		</c:forEach>
	</tr>
	<c:forEach var="minute" begin="0" end="${model.lastMinute}">
		<tr class="${minute%2==0 ? 'even' : 'odd'}">
			<td>${w:format(model.hour,'00')}:${w:format(minute,'00')}</td>
			<c:forEach var="thread" items="${threads}">
				<td>${a:showLegends(thread.value, minute)}</td>
			</c:forEach>
		</tr>
	</c:forEach>
</table>
<br>

<table class="legend">
</table>

<xmp>${report}</xmp>

</jsp:body>

</a:report>