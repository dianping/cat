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
<table class="problem">
	<tr>
		<th colspan="3">${model.problemStatistics.subTitle}</th>
	</tr>
	<tr>
		<td colspan="3"><a
			href="?op=detail&domain=${model.domain}&ip=${model.ipAddress}&minute=${model.minuteLast}&date=${model.date}${model.problemStatistics.url}"
			class="minute" onclick="return show(this);">上一分钟</a> &nbsp;&nbsp; <a
			href="?op=detail&domain=${model.domain}&ip=${model.ipAddress}&minute=${model.minuteNext}&date=${model.date}${model.problemStatistics.url}"
			class="minute" onclick="return show(this);">下一分钟</a>
			&nbsp;&nbsp;&nbsp;CurrentMinute: ${model.currentMinute}
		</td>
	</tr>
	<tr>
		<th>Type</th>
		<th>Count</th>
		<th>Detail</th>
	</tr>
	<c:forEach var="statistics" items="${model.problemStatistics.status}">
		<tr>
			<td><a href="#" class="${statistics.value.type}">&nbsp;&nbsp;</a>&nbsp;&nbsp;${statistics.value.type}
			</td>
			<td>${statistics.value.count}</td>
			<td>
				<table class="problem">
					<tr>
						<th width="20%">Status</th>
						<th width="10%">Count</th>
						<th width="70%">SampLinks</th>
					</tr>
					<c:forEach var="status" items="${statistics.value.status}">
						<tr>
							<td>${status.value.status}</td>
							<td>${status.value.count}</td>
							<td><c:forEach var="links" items="${status.value.links}">
									<a href="${model.logViewBaseUri}/${links}">Log</a>
								</c:forEach></td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</c:forEach>
</table>


