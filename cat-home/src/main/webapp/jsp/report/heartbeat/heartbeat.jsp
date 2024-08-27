<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"
	trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.heartbeat.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.heartbeat.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.heartbeat.Model" scope="request" />
<c:set var="report" value="${model.report}" />

<a:hourly_report title="HeartBeat Report" navUrlPrefix="ip=${model.ipAddress}&domain=${model.domain}" timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">
	<jsp:attribute name="subtitle">${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<table class="machines">
	<th style="text-align:left">
		<c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
				<c:when test="${payload.realIp eq ip}">
					<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}" class="current">${ip}</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}">${ip}</a>
				</c:otherwise>
			</c:choose>
   	 		&nbsp;]&nbsp;
		</c:forEach>
		</th>
</table>
<table>
<c:forEach items="${model.extensionGraph}" var="entry">
	<c:choose>
		<c:when test="${entry.key eq 'System'}">
			<tr><th><h5 class='text-error'>System</h5></th></tr>
		</c:when>
		<c:when test="${entry.key eq 'GC'}">
			<tr><th><h5 class='text-error'>GC</h5></th></tr>
		</c:when>
		<c:when test="${entry.key eq 'JVMHeap'}">
			<tr><th><h5 class='text-error'>Heap</h5></th></tr>
		</c:when>
		<c:when test="${entry.key eq 'FrameworkThread'}">
			<tr><th><h5 class='text-error'>Thread</h5></th></tr>
		</c:when>
		<c:when test="${entry.key eq 'Disk'}">
			<tr><th><h5 class='text-error'>Storage</h5></th></tr>
		</c:when>
		<c:when test="${entry.key eq 'CatUsage'}">
			<tr><th><h5 class='text-error'>CAT</h5></th></tr>
		</c:when>
		<c:otherwise>
			<!-- 如果键名不在上述列表中，则不显示 -->
		</c:otherwise>
	</c:choose>
	<c:if test="${entry.key != 'client-send-queue'}">
	<tr>
		<td>
		<c:set var="size" value="${entry.value.height}"/>
		<c:set var="extensionHeight" value="${size*190 }"/>
			<svg version="1.1" width="1200" height="${extensionHeight}" xmlns="http://www.w3.org/2000/svg">
				<c:forEach items="${entry.value.svgs}" var="kv">
					${kv.value}
				</c:forEach>
			</svg>
		</td>
	</tr>
    </c:if>
</c:forEach>
</table>

<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		appendHostname(${model.ipToHostnameStr});
	});
</script>
</jsp:body>
</a:hourly_report>
