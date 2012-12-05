<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.ip.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.ip.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.ip.Model" scope="request" />
<c:set var="report" value="${model.report}"/>

<a:report title="Top IP Report" navUrlPrefix="domain=${model.domain}" timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">
	
	<jsp:attribute name="subtitle">Ip Info In Last 15 Minutes</jsp:attribute>
<jsp:body>

<res:useCss value='${res.css.local.ip_css}' target="head-css"/>

<table class="ip-table">
	<tr><th>No.</th><th>IP</th><th>last 1 min</th><th>last 5 mins</th><th>last 15 mins</th><th>location</th></tr>
	<c:forEach var="m" items="${model.displayModels}" varStatus="status">
		<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
			<td>${status.index+1}</td>
			<td>${m.address}</td>
			<td>${m.lastOne}</td>
			<td>${m.lastFive}</td>
			<td>${m.lastFifteen}</td>
			<td>${a:getLocation(m.address)}</td>
		</tr>
	</c:forEach>
</table>

</jsp:body>
	
</a:report>
