<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.problem.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.problem.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.problem.Model" scope="request" />
<c:set var="report" value="${model.report}" />

<res:bean id="res"/>
<table class="table table-hover table-striped table-condensed">
	<tr>
		<td title="time\group">T\G</td>
			<c:forEach var="group" items="${model.groupLevelInfo.groups}"
						varStatus="status">
			<td title="${group}"> 
				<a href="?op=thread&domain=${model.domain}&ip=${model.ipAddress}&group=${group}&date=${model.date}" onclick="return requestGroupInfo(this)">${w:shorten(group, 20)}</a>
			</td>
		</c:forEach>
	</tr>
	<c:forEach var="minute" items="${model.groupLevelInfo.datas}"	varStatus="status">
		<tr class="">
			${minute}
		</tr>
	</c:forEach>
</table>
<br>


