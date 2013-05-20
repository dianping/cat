<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.aggregation.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.aggregation.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.aggregation.Model" scope="request"/>


<a:body>

	<res:useCss value='${res.css.local.alarm_css}' target="head-css" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		init();
	});
</script>
	
	<div>
			</br>
			<table class="project" id="contents" width="100%">
			<thead>
				<tr class="odd">
					<td width="15%">报表类型</td>
					<td width="10%">域名</td>
					<td width="10%">规则</td>
					<td width="15%">显示名称</td>
					<td width="8%">示例</td>
					<td width="5%">操作&nbsp;&nbsp;  <a href="?op=update" target="_blank">新增</a></td>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.aggregationRules}"
					varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<c:choose>
							<c:when test="${item.type == 1}">
								<td>transaction</td>
							</c:when>
							<c:when test="${item.type == 2}">
								<td>event</td>
							</c:when>
							<c:when test="${item.type == 3}">
								<td>problem
							</c:when>
						</c:choose>
						<td>${item.domain}</td>
						<td>${item.pattern}</td>
						<td>${item.displayName}</td>
						<td>${item.sample}</td>
						<td><a href="?op=update&id=${item.id}">编辑</a>
						<a href="?op=delete&id=${item.id}">删除</a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
		</div>
		
</a:body>