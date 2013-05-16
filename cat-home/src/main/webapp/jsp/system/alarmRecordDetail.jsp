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

<a:body>

	<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>
	

	<div class="container-fluid">
      	<div class="row-fluid">
        <div class="span2">
		<%@include file="./alarm.jsp"%>
		</div>
		<div class="span10">
			</br>
			<table border="1" rules="all">
				<tr>
					<td>&nbsp;&nbsp;收件人</td>
					<td>${model.mailRecord.receivers}</td>
				</tr>
				<tr>
					<td>发送时间</td>
					<td>${w:format(model.mailRecord.creationDate,'yyyy-MM-dd
						HH:mm:ss')}</td>
				</tr>
				<tr>
					<td>邮件标题</td>
					<td>${model.mailRecord.title}</td>
				</tr>
				<tr>
					<td>邮件内容</td>
					<td><div>${model.mailRecord.content}</div></td>
				</tr>
			</table>
		</div></div>
	</div>
</a:body>