<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context"
	scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model"
	scope="request" />
	
<script type="text/javascript">
	$(document).ready(function() {
		var id = '${payload.action.name}';
		$('#'+id).addClass("active");
	});
</script>

<a:application>

	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>
	

	<div class="container-fluid">
      	<div class="row-fluid">
        <div class="span2">
		<%@include file="./alarm.jsp"%>
		</div>
		<div class="span10">
			</br>
			<table  class="alarm table table-striped table-condensed   "  border="1" rules="all">
				<tr>
					<th>&nbsp;&nbsp;收件人</th>
					<td>${model.mailRecord.receivers}</td>
				</tr>
				<tr>
					<th>发送时间</th>
					<td>${w:format(model.mailRecord.creationDate,'yyyy-MM-dd
						HH:mm:ss')}</td>
				</tr>
				<tr>
					<th>邮件标题</th>
					<td>${model.mailRecord.title}</td>
				</tr>
				<tr>
					<th>邮件内容</th>
					<td><div>${model.mailRecord.content}</div></td>
				</tr>
			</table>
		</div></div>
	</div>
</a:application>