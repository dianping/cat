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

<a:report title="Problem Report" navUrlPrefix="domain=${model.domain}&ip=${model.ipAddress}" timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

<jsp:body>

<res:useCss value="${res.css.local.problem_css}" target="head-css"/>
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<res:useJs value="${res.js.local.problem_js}" target="head-js"/>

<table class="machines">
   <tr>
	  <th>Machines:</th>
   	  <c:forEach var="machine" items="${report.machines}">
   	  	<td><a href="?domain=${model.domain}&ip=${machine.value.ip}">${machine.value.ip}</a></td>
   	  </c:forEach>
   </tr>
</table>

<table class="problem">
	<tr><td title="time\group">T\G</td>
		<c:forEach var="group" items="${model.groupLevelInfo.groups}" varStatus="status">
			<td> <a href="?op=thread&domain=${model.domain}&ip=${model.ipAddress}&group=${group}&date=${model.date}">${group}</a></td>
		</c:forEach>
	</tr>
	<c:forEach var="minute" items="${model.groupLevelInfo.datas}" varStatus="status">
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'}">
			${minute}
		</tr>
	</c:forEach>
</table>
<br>

<table class="problem">
<tr>
		<th>Type</th>
		<th>Total</th>
		<th>Status</th>
		<th>Count</th>
		<th>SampleLinks</th>
	</tr>
	<c:forEach var="statistics" items="${model.allStatistics.status}">
		<tr>
			<td rowspan="${size}"><a href="#" class="${statistics.value.type}">&nbsp;&nbsp;</a>
				&nbsp;&nbsp;${statistics.value.type}
			</td>
			<td rowspan="${size}">${statistics.value.count}</td>
			<c:forEach var="status" items="${statistics.value.status}">
				<td>${status.value.status}</td>
				<td>${status.value.count}</td>
				<td><c:forEach var="links" items="${status.value.links}">
							<a href="${model.logViewBaseUri}/${links}">Log</a>
					</c:forEach>
				</td>
			</c:forEach>
			</tr>
	</c:forEach>
</table>

<table class="legend">
</table>

</jsp:body>

</a:report>