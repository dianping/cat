<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"
	type="com.dianping.cat.report.page.problem.Context" scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.problem.Payload" scope="request" />
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.problem.Model" scope="request" />
<c:set var="report" value="${model.report}" />
<c:set var="threads" value="${report.machines[model.ipAddress].threads}" />

<a:report title="Problem Report"
	navUrlPrefix="domain=${model.domain}&ip=${model.ipAddress}"
	timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

	<jsp:body>

<res:useCss value="${res.css.local.problem_css}" target="head-css" />
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />

<%@ include file="problemTable.jsp"%>

<c:if test="${model.ipAddress ne 'All'}">
<table class="problem">
	<tr>
		<td title="time\group">T\G</td>
		<c:forEach var="group" items="${model.groupLevelInfo.groups}"
						varStatus="status">
		<td title="${group}"> 
			<a
							href="?op=thread&domain=${model.domain}&ip=${model.ipAddress}&group=${group}&date=${model.date}">${w:shorten(group, 20)}</a>
					</td>
		</c:forEach>
	</tr>
	<c:forEach var="minute" items="${model.groupLevelInfo.datas}"
					varStatus="status">
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'}">
			${minute}
		</tr>
	</c:forEach>
</table>
<br>
</c:if>

<table class="legend">
</table>

<res:useJs value="${res.js.local.problem_js}" target="buttom-js" />
</jsp:body>

</a:report>