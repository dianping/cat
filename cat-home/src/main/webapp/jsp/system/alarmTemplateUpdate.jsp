<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model" scope="request" />

<form name="exceptionAlarmModify" id="form" method="post" action="${model.pageUri}?op=alarmTemplateUpdateSubmit">
	<table border="0">
		<input type="hidden" name="alarmTemplateId" value="${model.alarmTemplate.id}" />
		<tr>
			<td>模板名称</td>
			<td><input type="name" name="templateName" value="${model.alarmTemplate.name}" readonly/></td>
		</tr>
		<tr>
			<td>模板内容</td>
			<td><textarea style="height:500px;width:500px" id="content" name="content">${model.alarmTemplate.content}</textarea></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><input type="submit" name="submit" value="submit" /></td>
		</tr>
	</table>
</form>