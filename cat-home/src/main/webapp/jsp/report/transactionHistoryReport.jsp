<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />

<script>
	var urlResponseTrend = ${model.urlResponseTrend};
	var callResponseTrend = ${model.callResponseTrend};
	var sqlResponseTrend = ${model.sqlResponseTrend};	
	
	var urlHitTrend = ${model.urlHitTrend};
	var callHitTrend = ${model.callHitTrend};
	var sqlHitTrend = ${model.sqlHitTrend};
</script>

<a:historyReport title="History Report">
	<jsp:attribute name="subtitle">From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	<res:useCss value="${res.css.local.transaction_css}" target="head-css" />
	<res:useJs value="${res.js.local.flotr2_js}" target="head-js" />
</br>

<table class="machines">
	<tr style="text-align: left">
		<th>Machines:
   	  		 <c:forEach var="ip" items="${model.ips}">&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a		href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&reportType=${model.reportType}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a
									href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&reportType=${model.reportType}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<br>
<table class="transaction">
	<c:choose>
		<c:when test="${empty payload.type}">
		<tr>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=type">Type</a></th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=total">Total Count</a></th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=failure">Failure Count</a></th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=failurePercent">Failure%</a></th>
			<th>Sample Link</th>
						<th>Min(ms)</th>
						<th>Max(ms)</th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=avg">Avg</a>(ms)</th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=95line">95Line</a>(ms)</th>
			<th>Std(ms)</th>
						<th>TPS</th>
					</tr>
			<c:forEach var="item" items="${model.displayTypeReport.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td style="text-align: left"><a
								href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${item.type}">${item.type}</a></td>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent,'0.00')}</td>
					<td><a
								href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
					<td>${w:format(e.min,'0.#')}</td>
					<td>${w:format(e.max,'0.#')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.line95Value,'0.0')}</td>
					<td>${w:format(e.std,'0.0')}</td>
					<td>${w:format(e.tps,'0.0')}</td>
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<tr>
			<th>
			<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=type">Name</a>
						</th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=total">Total Count</a></th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=failure">Failure Count</a></th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=failurePercent">Failure%</a></th>
			<th>Sample Link</th>
						<th>Min(ms)</th>
						<th>Max(ms)</th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=avg">Avg</a>(ms)</th>
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=95line">95Line</a>(ms)</th>
			<th>Std(ms)</th>
						<th>TPS</th>
					</tr>
			<c:forEach var="item" items="${model.displayNameReport.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td style="text-align: left">${e.id}</td>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent,'0.00')}</td>
					<td><a
								href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
					<td>${w:format(e.min,'0.#')}</td>
					<td>${w:format(e.max,'0.#')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.line95Value,'0.0')}</td>
					<td>${w:format(e.std,'0.0')}</td>
					<td>${w:format(e.tps,'0.0')}</td>
				</tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>

</br>

<table>
	<tr>
		<td><div id="urlResponseTrend" class="graph"></div>	</td>
		<td><div id="urlHitTrend" class="graph"></div>	</td>
	</tr>
		<tr>
		<td><div id="callResponseTrend" class="graph"></div>	</td>
		<td><div id="callHitTrend" class="graph"></div>	</td>
	</tr>
		<tr>
		<td><div id="sqlResponseTrend" class="graph"></div>	</td>
		<td><div id="sqlHitTrend" class="graph"></div>	</td>
	</tr>
</table>

<res:useJs value="${res.js.local.transactionHistory_js}"
			target="1bottom-js" />
</jsp:body>

</a:historyReport>