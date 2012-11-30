<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.project.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.project.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.project.Model" scope="request"/>

<a:body>

	<res:useCss value='${res.css.local.alarm_css}' target="head-css" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	

<form name="projectUpdate" id="form" method="post" action="${model.pageUri}?op=updateSubmit">
	<table border="0">
		<input type="hidden" name="projectId" value="${model.project.id}" />
		<input type="hidden" name="domain" value="${model.project.domain}" />
		<tr>
			<td>项目名称</td>
			<td>${model.project.domain}</td>
			<td></td>
		</tr>
		<tr>
			<td>所属部门</td>
			<td><input type="name" name="department" value="${model.project.department}"/></td>
			<td style='color:red'>（一级分类）建议填写，主站、手机、团购、搜索、架构</td>
		</tr>
		<tr>
			<td>产品线</td>
			<td><input type="name" name="projectLine" value="${model.project.projectLine}"/></td>
			<td style='color:red'>（二级分类）由各自业务线决定,建议字数小于4</td>
		</tr>
		<tr>
			<td>负责人</td>
			<td><input type="name" name="owner" value="${model.project.owner}"/></td>
			<td>可选字段</td>
		</tr>
		<tr>
			<td>项目组邮件</td>
			<td><input type="name" name="email" size="50" value="${model.project.email}"/></td>
			<td>可选字段(多个，逗号分割)</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><input type="submit" name="submit" value="submit" /></td>
		</tr>
	</table>
</form>

</a:body>