<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<c:set var="report" value="${model.report}"/>


<a:report title="Transaction Report${empty payload.type ? '' : ' :: '}<a href='?domain=${model.domain}&date=${model.date}&type=${payload.type}'>${payload.type}</a>" navUrlPrefix="ip=${model.ipAddress}&domain=${model.domain}${empty payload.type ? '' : '&type='}${payload.type}" timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">

<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

<jsp:body>

<res:useCss value="${res.css.local.transaction_css}" target="head-css"/>
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<table class="machines">
	<tr style="text-align:left">
		<th>Machines: &nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?domain=${model.domain}&date=${model.date}"
						class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&date=${model.date}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}"
							class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}">${ip}</a>
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
		<tr><th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=type">Type</a></th>
			<th class="right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=total">Total Count</a></th>
			<th class="right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=failure">Failure Count</a></th>
			<th class="right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=failurePercent">Failure%</a></th>
			<th class="right">Sample Link</th><th>Min(ms)</th><th>Max(ms)</th>
			<th class="right"><a href="?domain=${model.domain}&date=${model.date}&sort=avg">Avg</a>(ms)</th>
			<th class="right"><a href="?domain=${model.domain}&date=${model.date}&sort=95line">95Line</a>(ms)</th>
			<th class="right">Std(ms)</th><th>TPS</th></tr>
			<c:forEach var="item" items="${model.displayTypeReport.results}" varStatus="status">
				<c:set var="e" value="${item.detail}"/>
				<c:set var="lastIndex" value="${status.index}"/>
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td style="text-align:left"><a href="?domain=${report.domain}&date=${model.date}&ip=${model.ipAddress}&type=${item.type}">${item.type}</a></td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent,'0.00')}</td>
					<td><a href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
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
			<input type="text" name="queryname" id="queryname"  value="${model.queryName}">
		    <input  style="WIDTH: 60px" value="Filter" onclick="selectByName('${model.date}','${model.domain}','${model.ipAddress}','${payload.type}')" type="submit">
			<tr>
			<th><a href="?op=graphs&domain=${report.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}" class="graph_link" data-status="-1">[:: show ::]</a>
			<a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}&sort=type">Name</a></th>
			<th  class="right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}&sort=total">Total Count</a></th>
			<th  class="right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}&sort=failure">Failure Count</a></th>
			<th  class="right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}&sort=failurePercent">Failure%</a></th>
			<th  class="right">Sample Link</th><th>Min(ms)</th><th>Max(ms)</th>
			<th  class="right"><a href="?domain=${model.domain}&date=${model.date}&type=${payload.type}&sort=avg">Avg</a>(ms)</th>
			<th  class="right"><a href="?domain=${model.domain}&date=${model.date}&type=${payload.type}&sort=95line">95Line</a>(ms)</th>
			<th class="right">Std(ms)</th><th>TPS</th></tr>
			<tr class="graphs"><td colspan="6"><div id="-1" style="display:none"></div></td></tr>
			<c:forEach var="item" items="${model.displayNameReport.results}" varStatus="status">
				<c:set var="e" value="${item.detail}"/>
				<c:set var="lastIndex" value="${status.index}"/>
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td width="25%" style="text-align:left;word-wrap:break-word;word-break:break-all;"><a href="?op=graphs&domain=${report.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}&name=${e.id}" class="graph_link" data-status="${status.index}">[:: show ::]</a> 
					${w:shorten(e.id, 50)}</td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent,'0.00')}</td>
					<td><a href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
					<td>${w:format(e.min,'0.#')}</td>
					<td>${w:format(e.max,'0.#')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.line95Value,'0.0')}</td>
					<td>${w:format(e.std,'0.0')}</td>
					<td>${w:format(e.tps,'0.0')}</td>
				</tr>
				<tr class="graphs"><td colspan="11"><div id="${status.index}" style="display:none"></div></td></tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>
<font color="white">${lastIndex+1}</font>

<res:useJs value="${res.js.local.transaction_js}" target="bottom-js" />
</jsp:body>

</a:report>
