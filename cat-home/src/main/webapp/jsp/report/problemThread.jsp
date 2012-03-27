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

<a:report title="Problem Report" navUrlPrefix="op=thread&group=${model.groupName}&domain=${model.domain}&ip=${model.ipAddress}" timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

<jsp:body>

<res:useCss value="${res.css.local.problem_css}" target="head-css"/>
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<res:useJs value="${res.js.local.problem_js}" target="head-js"/>

<table class="machines">
   <tr>
	  <th>Machines:</th>
   	  <c:forEach var="machine" items="${report.machines}">
   	  	<td><a href="?domain=${model.domain}&ip=${model.ipAddress}">${machine.value.ip}</a></td>
   	  </c:forEach>
   </tr>
</table>

<table class="problem">
	<tr><td title="time\group">T\G</td>
		<c:forEach var="group" items="${model.threadLevelInfo.groups}" varStatus="status">
			<td colspan="${group.number}"><a href="?op=thread&domain=${model.domain}&ip=${model.ipAddress}&group=${group.name}&date=${model.date}">${group.name}</a></td>
		</c:forEach>
	</tr>
	<tr><td title="time\thread">T\T</td>
		<c:forEach var="thread" items="${model.threadLevelInfo.threads}" varStatus="status">
			<td>${thread}</td>
		</c:forEach>
	</tr>
	<c:forEach var="minute" items="${model.threadLevelInfo.datas}" varStatus="status">
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'}">
			${minute}
		</tr>
	</c:forEach>
</table>
<br>
<table class="problem">
<tr>
		<th>Type</th>
		<th>Count</th>
		<th>Detail</th>
	</tr>
	<c:forEach var="statistics" items="${model.allStatistics.status}">
		<tr>
			<td><a href="#" class="${statistics.value.type}">&nbsp;&nbsp;</a>&nbsp;&nbsp;${statistics.value.type}
			</td>
			<td>${statistics.value.count}</td>
			<td>
				<table class="problem">
					<tr>
						<th width="20%">Status</th>
						<th width="10%">Count</th>
						<th width="70%">SampLinks</th>
					</tr>
					<c:forEach var="status" items="${statistics.value.status}">
						<tr>
							<td>${status.value.status}</td>
							<td>${status.value.count}</td>
							<td><c:forEach var="links" items="${status.value.links}">
									<a href="${model.logViewBaseUri}/${links}">Log</a>
								</c:forEach></td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</c:forEach>
</table>
<table class="legend">
</table>

</jsp:body>

</a:report>