<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.historyReport.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.historyReport.Payload"	scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.historyReport.Model" scope="request" />

<a:simpleReport title="History Report">
	<jsp:body>
	<res:useCss value='${res.css.local.jqueryUI_css}' target="head-css" />
	<res:useCss value='${res.css.local.calendar_css}' target="head-css" />
	<res:useJs value="${res.js.local.jqueryMin_js}" target="head-js" />
	<res:useJs value="${res.js.local.jqueryUIMin_js}" target="head-js" />
	<res:useJs value="${res.js.local.datepicker_js}" target="head-js" />

<%@ include file="historyReport.jsp"%>
</br>

<table class="machines">
	<tr style="text-align:left">
		<th>Machines: 
			<c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?op=problem&domain=${model.domain}&ip=${ip}&startDate=${payload.startDate}&endDate=${payload.endDate}&threshold=${model.threshold}"
							class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?op=problem&domain=${model.domain}&ip=${ip}&startDate=${payload.startDate}&endDate=${payload.endDate}&threshold=${model.threshold}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th><th>long-url <select size="1" id="p_longUrl">
				<option value="1000">1.0 Sec</option>
				<option value="1500">1.5 Sec</option>
				<option value="2000">2.0 Sec</option>
				<option value="3000">3.0 Sec</option>
				<option value="4000">4.0 Sec</option>
				<option value="5000">5.0 Sec</option>
		</select> 
		<script>
			var threshold='${model.threshold}';
			$("#p_longUrl").val(threshold) ;
		</script>
	<input style="WIDTH: 60px" value="Refresh"
			onclick="longTimeChange('${model.domain}','${model.ipAddress}')"
			type="submit">
		</th>
	</tr>
</table>

<br>
<table>
	<tr>
		<th>Type</th>
		<th>Total</th>
		<th>Status</th>
		<th>Count</th>
		<th>SampleLinks</th>
	</tr>
	<c:forEach var="statistics" items="${model.problemStatistics.status}"
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

<res:useJs value="${res.js.local.historyReport_js}" target="bottom-js" />
</jsp:body>

</a:simpleReport>