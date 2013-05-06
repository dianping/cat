<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.cache.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.cache.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.cache.Model" scope="request" />
<c:set var="report" value="${model.report}"/>

<a:report title="Cache Report${empty payload.type ? '' : ' :: '}<a href='?domain=${model.domain}&date=${model.date}&type=${payload.type}'>${payload.type}</a>" navUrlPrefix="ip=${model.ipAddress}&queryname=${model.queryName}&domain=${model.domain}${empty payload.type ? '' : '&type='}${payload.type}" timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">

<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

<jsp:body>

<res:useCss value="${res.css.local.cache_css}" target="head-css" />
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
</br>
<table class="machines">
	<tr style="text-align:left">
		<th>Machines: &nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?domain=${model.domain}&date=${model.date}&type=${payload.type}&queryname=${model.queryName}"
						class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&date=${model.date}&type=${payload.type}&queryname=${model.queryName}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}&type=${payload.type}&queryname=${model.queryName}"
							class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}&type=${payload.type}&queryname=${model.queryName}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<br>
<table class="cache">
    <c:choose>
		<c:when test="${empty payload.type}">
		<tr><th class="left"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=type">Type</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=total">Total</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=missed">Missed</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=hitPercent">Hit Rate(%)</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&sort=avg">Avg</a>(ms)</th>
			<th>QPS</th></tr>
			<c:forEach var="item" items="${model.report.typeItems}" varStatus="status">
				<c:set var="e" value="${item.type}"/>
				<c:set var="lastIndex" value="${status.index}"/>
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td style="text-align:left"><a href="?domain=${report.domain}&date=${model.date}&ip=${model.ipAddress}&type=${e.id}">${e.id}</a></td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${item.missed}</td>
					<td>${w:format(item.hited,'0.00%')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.tps,'0.0')}</td>
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<tr><th class="left" colspan='6'><input type="text" name="queryname" id="queryname" size="40" value="${model.queryName}">
		    <input id="queryname" style="WIDTH: 60px"  onclick="filterByName('${model.date}','${model.domain}','${model.ipAddress}','${payload.type}')" type="submit">
			支持多个字符串查询，例如sql|url|task，查询结果为包含任一sql、url、task的列
			</th></tr>
			<script>
			function filterByName(date,domain,ip){
				var queryname=$("#queryname").val();
				window.location.href="?domain="+domain+"&type="+type+"&date="+date+"&queryname="+queryname+"&ip="+ip;
			}
		</script>
			<tr>
			<th  class="left">
			<a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}&sort=type&queryname=${model.queryName}">Name</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}&sort=total&queryname=${model.queryName}">Total</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}&sort=missed&queryname=${model.queryName}">Missed</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&type=${payload.type}&sort=hitPercent&queryname=${model.queryName}">Hit Rate(%)</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&type=${payload.type}&sort=avg&queryname=${model.queryName}">Avg</a>(ms)</th>
			<th>QPS</th></tr>
			<c:forEach var="item" items="${model.report.nameItems}" varStatus="status">
				<c:set var="e" value="${item.name}"/>
				<c:set var="lastIndex" value="${status.index}"/>
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td style="text-align:left;word-wrap:break-word;word-break:break-all;">${w:shorten(e.id, 80)}</td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${item.missed}</td>
					<td>${w:format(item.hited,'0.00%')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.tps,'0.0')}</td>
				</tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>
<font color="white">${lastIndex+1}</font>

</jsp:body>

</a:report>
