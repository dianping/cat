<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.problem.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.problem.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.problem.Model" scope="request" />
<c:set var="report" value="${model.report}"/>

<a:report title="Problem Detail Report" navUrlPrefix="op=detail&domain=${model.domain}&ip=${model.ipAddress}&thread=${model.threadId}&minute=${model.currentMinute}" timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}  Minute:${model.currentMinute}
		&nbsp;&nbsp;
		<a href="?op=detail&domain=${model.domain}&ip=${model.ipAddress}&thread=${model.threadId}&minute=${model.minuteLast}&date=${model.date}">上一分钟</a>
		&nbsp;&nbsp;
		<a href="?op=detail&domain=${model.domain}&ip=${model.ipAddress}&thread=${model.threadId}&minute=${model.minuteNext}&date=${model.date}">下一分钟</a>
</jsp:attribute>

<jsp:body>

<res:useCss value="${res.css.local.problem_css}" target="head-css"/>
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<res:useJs value="${res.js.local.problem_js}" target="head-js"/>
<table class="problem">
	<tr><th>Type</th><th>Count</th><th>Detail</th></tr>
	<c:forEach var="statistics" items="${model.statistics}">
	<tr>
		<td><a href="#" class="${statistics.value.type}" >&nbsp;&nbsp;</a>&nbsp;&nbsp;${statistics.value.type}
			</td>
			<td>${statistics.value.count}</td>
			<td>
				<table class="problem">
					<tr><th width="20%">Status</th><th  width="10%">Count</th><th  width="70%">SampLinks</th></tr>
					<c:forEach var="status" items="${statistics.value.status}">
						<tr>
							<td>${status.value.status}</td>
							<td>${status.value.count}</td>
							<td> 
								<c:forEach var="links" items="${status.value.links}" >
									<a href="${model.logViewBaseUri}/${links}">Log</a>
								</c:forEach>
							</td>
						</tr>
					</c:forEach>		
				</table>
			</td>
			</tr>
	</c:forEach>
</table>
<br>

<table class="legend">
</table>
</jsp:body>
</a:report>


