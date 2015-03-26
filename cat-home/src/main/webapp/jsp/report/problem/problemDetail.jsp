<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"
	type="com.dianping.cat.report.page.problem.Context" scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.problem.Payload" scope="request" />
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.problem.Model" scope="request" />
<c:set var="report" value="${model.report}" />
<table class="table table-hover table-striped table-condensed">
	<tr>
		<th colspan="5">${model.detailStatistics.subTitle}</th>
	</tr>
	<tr>
		<td colspan="5"><a
			href="?op=detail&domain=${model.domain}&ip=${model.ipAddress}&minute=${model.minuteLast}&date=${model.date}${model.detailStatistics.url}"
			class="minute" onclick="return show(this);">上一分钟</a> &nbsp;&nbsp; <a
			href="?op=detail&domain=${model.domain}&ip=${model.ipAddress}&minute=${model.minuteNext}&date=${model.date}${model.detailStatistics.url}"
			class="minute" onclick="return show(this);">下一分钟</a>
			&nbsp;&nbsp;&nbsp;CurrentMinute: ${model.currentMinute}
		</td>
	</tr>
	<tr>
		<th>Type</th>
		<th>Total</th>
		<th>Status</th>
		<th>Count</th>
		<th>SampleLinks</th>
	</tr>
	<c:forEach var="statistics" items="${model.detailStatistics.status}" varStatus="typeIndex">
		<tr>
			<td rowspan="${w:size(statistics.value.status)}"><a href="#"
						class="${statistics.value.type}">&nbsp;&nbsp;</a>
				&nbsp;&nbsp;${statistics.value.type}
			</td>
			<td rowspan="${w:size(statistics.value.status)}">${statistics.value.count}</td>
			<c:forEach var="status" items="${statistics.value.status}"
						varStatus="index">
				<c:if test="${index.index != 0}">
					<tr>
				</c:if>
				<td >${status.value.status}</td>
				<td >${status.value.count}</td>
				<td >
					<c:forEach var="links" items="${status.value.links}" varStatus="linkIndex"><a href="/cat/r/m/${links}?domain=${model.domain}">${linkIndex.first?'L':(linkIndex.last?'g':'o')}</a></c:forEach>
				</td>
				<c:if test="${index.index != 0}">
				</tr>
				</c:if>
			</c:forEach>
			</tr>
	</c:forEach>
</table>



