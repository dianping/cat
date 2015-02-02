<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model" scope="request" />

<form name="scheduledReportModify" id="form" method="post" action="${model.pageUri}?op=scheduledReportUpdateSubmit">
	<table border="0">
		<input type="hidden" name="scheduledReportId" value="${model.scheduledReport.id}" />
		<tr>
			<td>项目名称</td>
			<td>${model.scheduledReport.domain}</td>
		</tr>
		<tr>
			<td>订阅内容</td>
			<td><textarea style="height:200px;width:200px" id="content" name="content">${model.scheduledReport.names}</textarea></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><input type="submit" name="submit" value="提交" /></td>
		</tr>
	</table>
</form>