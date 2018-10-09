<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context"	scope="request" />
<jsp:useBean id="payload"type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model"scope="request" />

<a:application>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			var id = '${payload.action.name}';
			$('#'+id).addClass("active");
		});
	</script>
	<div style="height:24px"></div>
      	<div class="row-fluid">
        <div class="span2">
		<%@include file="./alarm.jsp"%>
		</div>
		<div class="span10">
			<table class="alarm table table-striped table-condensed   " id="contents" width="100%">
				<thead>
				<tr >
					<th><span class="text-success">邮件类型</span></th>
					<th><span class="text-success">邮件标题</span></th>
					<th><span class="text-success">发送时间</span></th>
					<th><span class="text-success">详细信息</span></th>
				</tr></thead><tbody>
				<c:forEach var="item" items="${model.mailRecords}"
					varStatus="status">
					<tr class="">
						<td>
							<c:if test="${item.type == 1}">日常报表</c:if>
							<c:if test="${item.type == 2}">异常告警</c:if>
							<c:if test="${item.type == 3}">服务告警</c:if>
						</td>
						<td>${item.title}</td>
						<td>${w:format(item.creationDate,'yyyy-MM-dd HH:mm:ss')}</td>
						<td><a  class="btn btn-primary btn-sm" href="?op=alarmRecordDetail&alarmRecordId=${item.id}">详情</a></td>
					</tr>
				</c:forEach></tbody>
			</table>
		</div></div>
</a:application>