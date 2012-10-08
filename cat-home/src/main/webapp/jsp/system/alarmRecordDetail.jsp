<%@ page contentType="text/html; charset=utf-8"%>
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

<table border="0">
	<tr>
		<td>&nbsp;&nbsp;收件人</td>
		<td>${model.mailRecord.receivers}</td>
	</tr>
	<tr>
		<td>发送时间</td>
		<td>${model.mailRecord.sendtime}</td>
	</tr>
	<tr>
		<td>邮件标题</td>
		<td>${model.mailRecord.title}</td>
	</tr>
	<tr>
		<td>邮件内容</td>
		<td><textarea style="height: 500px; width: 500px" readonly>${model.mailRecord.content}</textarea></td>
	</tr>
</table>
