<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model" scope="request" />


<a:application>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>
	
	<script type="text/javascript">
		$(document).ready(function() {
			var id = 'alarmTemplateList'+'${model.alarmTemplate.name}';
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
			</br>
			<form name="exceptionAlarmModify" method="post" action="${model.pageUri}?op=alarmTemplateUpdateSubmit">
				<table>
					<input type="hidden" name="alarmTemplateId" value="${model.alarmTemplate.id}" />
					<tr>
						<th><span class="text-success">模板名称</span></th>
						<td><input type="name" name="templateName" value="${model.alarmTemplate.name}" readonly/></td>
					</tr>
					<tr>
						<th><span class="text-success">模板内容</span></th>
						<td><textarea style="height:300px;width:500px" id="content" name="content">${model.alarmTemplate.content}</textarea></td>
					</tr>
					<tr>
						<td colspan="2" align="center"><input class="btn btn-primary" type="submit" name="submit" value="提交"></td>
					</tr>
				</table>
			</form>
		</div></div>
	</div>
</a:application>