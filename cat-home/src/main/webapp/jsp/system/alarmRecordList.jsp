<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context"	scope="request" />
<jsp:useBean id="payload"type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model"scope="request" />

<a:body>

	<res:useJs value="${res.js.local['dtree.js']}" target="head-js" />
	<res:useCss value='${res.css.local.dtree_css}' target="head-css" />
	<res:useCss value='${res.css.local.alarm_css}' target="head-css" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />

	<div class="body-content">
		<%@include file="./alarm.jsp"%>
		<script type="text/javascript">
			d.openAll();
			d.s(${model.templateIndex});
		</script>
		<div class="content-right">
			</br>
			<table class="alarm" width="100%">
				<tr class="odd">
					<td>标题</td>
					<td>时间</td>
					<td>操作</td>
				</tr>
				<c:forEach var="item" items="${model.mailRecords}"
					varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${item.title}</td>
						<td>${item.sendtime}</td>
						<td><a href="?op=alarmRecordDetail&alarmRecordId=${item.id}" target="_blank">详情</a></td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</a:body>