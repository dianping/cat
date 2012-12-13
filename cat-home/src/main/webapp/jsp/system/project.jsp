<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.project.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.project.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.project.Model" scope="request"/>

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
					<td width="15%">项目名称</td>
					<td width="10%">所属部门</td>
					<td width="10%">产品线</td>
					<td width="15%">组邮件</td>
					<td width="8%">负责人</td>
					<td width="5%">操作</td>
				</tr></thead><tbody>
				<c:forEach var="item" items="${model.projects}"
					varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${item.domain}</td>
						<td>${item.department}</td>
						<td>${item.projectLine}</td>
						<td>${item.email}</td>
						<td>${item.owner}</td>
						<td><a href="?op=update&projectId=${item.id}">编辑</a></td>
					</tr>
				</c:forEach></tbody>
			</table>
		</div>
		
</a:body>