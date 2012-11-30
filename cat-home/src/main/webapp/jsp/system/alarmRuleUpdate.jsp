<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model" scope="request" />

<form name="alarmModify" id="form" method="post" action="${model.pageUri}?op=alarmRuleUpdateSubmit">
	<table border="0">
		<input type="hidden" name="alarmRuleId" value="${model.alarmRule.id}" />
		<tr>
			<td>项目名称</td>
			<td>${model.alarmRule.domain}</td>
		</tr>
		<tr>
			<td>自定义内容</td>
			<td><textarea style="height:500px;width:500px" id="content" name="content">${model.alarmRule.content}</textarea></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><input type="submit" name="submit" value="submit" /></td>
		</tr>
	</table>
</form>