<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.problem.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.problem.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.problem.Model" scope="request" />
<c:set var="report" value="${model.report}" />

<a:report title="Problem Report"
	navUrlPrefix="op=${payload.action.name}&domain=${model.domain}&ip=${model.ipAddress}&threshold=${model.threshold}"
	timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

	<jsp:body>

<res:useCss value="${res.css.local.problem_css}" target="head-css" />
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />

<table class="machines">
	<tr style="text-align:left">
		<th>Machines: &nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?domain=${model.domain}&date=${model.date}&threshold=${model.threshold}"
						class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&date=${model.date}&threshold=${model.threshold}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?op=view&domain=${model.domain}&ip=${ip}&date=${model.date}&threshold=${model.threshold}"
							class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?op=view&domain=${model.domain}&ip=${ip}&date=${model.date}&threshold=${model.threshold}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th><th>long-url <input id="thresholdInput" style="display: none"
			value="${model.threshold}"> <select size="1" id="p_longUrl">
				${model.defaultThreshold}
				<option value="500">0.5 Sec</option>
				<option value="1000">1.0 Sec</option>
				<option value="1500">1.5 Sec</option>
				<option value="2000">2.0 Sec</option>
				<option value="3000">3.0 Sec</option>
				<option value="4000">4.0 Sec</option>
				<option value="5000">5.0 Sec</option>
		</select> long-sql
		<select size="1" id="p_longSql">
				${model.defaultSqlThreshold}
				<option value="100">100 ms</option>
				<option value="500">500 ms</option>
				<option value="1000">1000 ms</option>
		</select>
		<script>
			var threshold='${model.threshold}';
			$("#p_longUrl").val(threshold) ;
			
			var sqlThreshold='${model.sqlThreshold}';
			$("#p_longSql").val(sqlThreshold) ;
			function longTimeChange(date,domain,ip){
				var longtime=$("#p_longUrl").val();
				var longSqlTime=$("#p_longSql").val();
				window.location.href="?op=view&domain="+domain+"&ip="+ip+"&date="+date+"&threshold="+longtime+"&sqlThreshold="+longSqlTime;
			}
		</script>
		
		<input style="WIDTH: 60px" value="Refresh"
			onclick="longTimeChange('${model.date}','${model.domain}','${model.ipAddress}')"
			type="submit">
		</th>
	</tr>
</table>

<br>
<table class="problem">
	<tr>
		<th>Type</th>
		<th>Total</th>
		<th>Status</th>
		<th>Count</th>
	</tr>
	<c:forEach var="statistics" items="${model.allStatistics.status}"
		varStatus="typeIndex">
		<tr>
			<td rowspan="${w:size(statistics.value.status)}"
				class="${typeIndex.index mod 2 != 0 ? 'even' : 'odd'} top"><a
				href="#" class="${statistics.value.type}">&nbsp;&nbsp;</a>
				&nbsp;&nbsp;${statistics.value.type}</td>
			<td rowspan="${w:size(statistics.value.status)}"
				class="${typeIndex.index mod 2 != 0 ? 'even' : 'odd'} top">${statistics.value.count}</td>
			<c:forEach var="status" items="${statistics.value.status}"
				varStatus="index">
				<c:if test="${index.index != 0}">
					<tr>
				</c:if>
				<td class="${index.index mod 2 != 0 ? 'even' : 'odd'}">${status.value.status}</td>
				<td class="${index.index mod 2 != 0 ? 'even' : 'odd'}">${status.value.count}</td>
				<td class="${index.index mod 2 != 0 ? 'even' : 'odd'}"><c:forEach
						var="links" items="${status.value.links}" varStatus="linkIndex">
						<a href="${model.logViewBaseUri}/${links}">${linkIndex.first?'L':(linkIndex.last?'g':'o')}</a>
					</c:forEach></td>
				<c:if test="${index.index != 0}">
		</tr>
		</c:if>
	</c:forEach>
	</tr>
	</c:forEach>
</table>
<br>

<c:if test="${model.ipAddress ne 'All'}">
<a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=group" onclick="return requestGroupInfo(this)">Threads Details</a>

<div id="machineThreadGroupInfo"></div>
<br>
</c:if>

<table class="legend">
</table>

<res:useJs value="${res.js.local.problem_js}" target="buttom-js" />
</jsp:body>

</a:report>