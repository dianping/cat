<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context"	scope="request" />
<jsp:useBean id="payload"type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model"scope="request" />

<a:body>

	
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	
	<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>

	<script type="text/javascript">
		$(document).ready(function() {
			init();
			var id = '${payload.action.name}';
			$('#'+id).addClass("active");
		});
	</script>
	<div class="container-fluid">
      	<div class="row-fluid">
        <div class="span2">
		<%@include file="./alarm.jsp"%>
		</div>
		<div class="span10">
			</br>
			<table class="alarm table table-striped table-bordered  table-condensed" id="contents" width="100%">
				<thead>
				<tr class="odd">
					<td>邮件类型</td>
					<td>邮件标题</td>
					<td>发送时间</td>
					<td>详细信息</td>
				</tr></thead><tbody>
				<c:forEach var="item" items="${model.mailRecords}"
					varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>
							<c:if test="${item.type == 1}">日常报表</c:if>
							<c:if test="${item.type == 2}">异常告警</c:if>
							<c:if test="${item.type == 3}">服务告警</c:if>
						</td>
						<td>${item.title}</td>
						<td>${w:format(item.creationDate,'yyyy-MM-dd HH:mm:ss')}</td>
						<td><a  class="btn btn-primary btn-small" href="?op=alarmRecordDetail&alarmRecordId=${item.id}">详情</a></td>
					</tr>
				</c:forEach></tbody>
			</table>
		</div></div>
	</div>
</a:body>