<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.alert.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.alert.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.alert.Model" scope="request" />

<a:navbar title="AlertReport" navUrlPrefix="">
	<jsp:body>
		<table	class="problem table table-striped table-bordered table-condensed table-hover">
			<tr class="text-success">
				<th width="10%">时间</th>
				<th width="5%">类型</th>
				<th width="5%">级别</th>
				<th width="10%">项目</th>
				<th width="10%">指标</th>
				<th width="60%">内容</th>
			</tr>
			<c:forEach var="entry" items="${model.alerts}" varStatus="status">
				<c:forEach var="alert" items="${entry.value}" varStatus="status">
					<tr>
						<c:if test="${status.first eq 'true'}">
							<td rowspan="${fn:length(entry.value)}">${entry.key}</td>
						</c:if>
						<td class="aleration_${alert.category}">${alert.category}</td>
						<td class="aleration_${alert.category}">${alert.type}</td>
						<td class="aleration_${alert.category}">${alert.domain}</td>
						<td class="aleration_${alert.category}">${alert.metric}</td>
						<td class="aleration_${alert.category}">${alert.content}</td>
					</tr>
				</c:forEach>
			</c:forEach>
		</table>
	</jsp:body>
</a:navbar>