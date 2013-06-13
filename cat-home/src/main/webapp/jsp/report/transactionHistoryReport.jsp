<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />

<a:historyReport title="History Report" navUrlPrefix="type=${payload.type}&queryname=${model.queryName}">
	<jsp:attribute name="subtitle">From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	<res:useCss value="${res.css.local.transaction_css}" target="head-css" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['flotr2_js']}" target="head-js"/>
	<res:useJs value="${res.js.local['baseTools_js']}" target="head-js"/>
	<res:useJs value="${res.js.local['trendGraph_js']}" target="head-js"/>
</br>
<table class="machines">
	<tr style="text-align: left">
		<th>Machines:
   	  		 <c:forEach var="ip" items="${model.ips}">&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&type=${payload.type}&queryname=${model.queryName}&reportType=${model.reportType}${model.customDate}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&type=${payload.type}&queryname=${model.queryName}&reportType=${model.reportType}${model.customDate}">${ip}</a>
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
			<th><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=type${model.customDate}">Type</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=total${model.customDate}">Total Count</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=failure${model.customDate}">Failure Count</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=failurePercent${model.customDate}">Failure%</a></th>
			<th class="right">Sample Link</th>
						<th class="right">Min(ms)</th>
						<th class="right">Max(ms)</th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=avg${model.customDate}">Avg</a>(ms)</th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&sort=95line${model.customDate}">95Line</a>(ms)</th>
			<th class="right">Std(ms)</th>
						<th class="right">QPS</th>
					</tr>
			<c:forEach var="item" items="${model.displayTypeReport.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td style="text-align: left">
							<a href="?op=historyGraph&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${item.type}${model.customDate}" class="history_graph_link" data-status="${status.index}">[:: show ::]</a>
							<a href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${item.type}${model.customDate}">${item.type}</a></td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent/100,'0.0000%')}</td>
					<td><a	href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${model.domain}">Log View</a></td>
					<td>${w:format(e.min,'0.#')}</td>
					<td>${w:format(e.max,'0.#')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.line95Value,'0.0')}</td>
					<td>${w:format(e.std,'0.0')}</td>
					<td>${w:format(e.tps,'0.0')}</td>
				</tr>
				<tr class="graphs"><td colspan="11"><div id="${status.index}" style="display:none"></div></td></tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
		<tr><th colspan='11'>
			<input type="text" id="queryname" size="40"  value="${model.queryName}">
		    <input style="WIDTH: 60px" value="Filter" onclick="filterByName('${model.date}','${model.domain}','${model.ipAddress}','${payload.type}')" type="submit">
		    支持多个字符串查询，例如sql|url|task，查询结果为包含任一sql、url、task的列
		</th></tr><script>
			function filterByName(date,domain,ip,type){
				var customDate ='${model.customDate}';
				var reportType = '${model.reportType}';
				var type = '${payload.type}';
				var queryname=$("#queryname").val();
				window.location.href="?op=history&domain="+domain+"&ip="+ip+"&date="+date+'&type='+type+'&reportType='+reportType+"&queryname="+queryname+customDate;
			}
		</script>
			<tr>
			<th>
			<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=type${model.customDate}&queryname=${model.queryName}">Name</a>
						</th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=total${model.customDate}&queryname=${model.queryName}">Total Count</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=failure${model.customDate}&queryname=${model.queryName}">Failure Count</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=failurePercent${model.customDate}&queryname=${model.queryName}">Failure%</a></th>
			<th class="right">Sample Link</th>
						<th class="right">Min(ms)</th>
						<th class="right">Max(ms)</th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=avg${model.customDate}&queryname=${model.queryName}">Avg</a>(ms)</th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=95line${model.customDate}&queryname=${model.queryName}">95Line</a>(ms)</th>
			<th class="right">Std(ms)</th>
						<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=total${model.customDate}&queryname=${model.queryName}">QPS</a></th>
						<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&sort=total${model.customDate}&queryname=${model.queryName}">Percent%</a></th>
					</tr>
			<c:forEach var="item" items="${model.displayNameReport.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td class="longText" style="text-align:left;white-space:normal">
					<c:choose>
					<c:when test="${status.index > 0}">
					<a href="?op=historyGraph&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${payload.type}&name=${e.id}${model.customDate}" class="history_graph_link" data-status="${status.index}">[:: show ::]</a> 
					</c:when>
					<c:otherwise></c:otherwise></c:choose>
					${w:shorten(e.id, 120)}</td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent/100,'0.0000%')}</td>
					<td><a	href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${model.domain}">Log View</a></td>
					<td>${w:format(e.min,'0.#')}</td>
					<td>${w:format(e.max,'0.#')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.line95Value,'0.0')}</td>
					<td>${w:format(e.std,'0.0')}</td>
					<td>${w:format(e.tps,'0.0')}</td>
					<td>${w:format(e.totalPercent,'0.00%')}</td>
				</tr>
				<tr class="graphs"><td colspan="12"><div id="${status.index}" style="display:none"></div></td></tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>

<font color="white">${lastIndex+1}</font>
<res:useJs value="${res.js.local.transaction_js}" target="bottom-js" />
<c:choose>
	<c:when test="${not empty payload.type}">
		<table>
			<tr>
				<td><div id="transactionGraph" class="pieChart"></div>
				</td>
			</tr>
		</table>
		<script type="text/javascript">
			var data = ${model.pieChart};
			graphPieChart(document.getElementById('transactionGraph'), data);
		</script>
	</c:when>
</c:choose>
</br>
</jsp:body>

</a:historyReport>