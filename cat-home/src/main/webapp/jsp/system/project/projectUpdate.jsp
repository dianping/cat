<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#projectList').addClass('active');
		});
	</script>
	
	<div>
		<div class="row-fluid">
        <div class="span2">
		<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
		</br>

<form name="projectUpdate" id="form" method="get" action="${model.pageUri}?op=updateSubmit">
	<table class="table table-striped table-bordered table-condensed">
		<input type="hidden" name="project.id" value="${model.project.id}" />
		<input type="hidden" name="project.domain" value="${model.project.domain}" />
		<input type="hidden" name="op" value="updateSubmit" />
		<tr>
			<td>项目名称</td>
			<td>${model.project.domain}</td>
			<td></td>
		</tr>
		<tr>
			<td>CMDB项目名称</td>
			<td><input type="name" name="project.cmdbDomain" value="${model.project.cmdbDomain}" required/></td>
			<td>cmdb中项目统一名称</td>
		</tr>
		<tr>
			<td>所属部门</td>
			<td><input type="name" name="project.department" value="${model.project.department}" required/></td>
			<td style='color:red'>（一级分类）建议填写，主站、手机、团购、搜索、架构</td>
		</tr>
		<tr>
			<td>产品线</td>
			<td><input type="name" name="project.projectLine" value="${model.project.projectLine}" required/></td>
			<td style='color:red'>（二级分类）由各自业务线决定,建议字数小于4</td>
		</tr>
		<tr>
			<td>负责人</td>
			<td><input type="name" name="project.owner" value="${model.project.owner}"/></td>
			<td>可选字段</td>
		</tr>
		<tr>
			<td>项目组邮件</td>
			<td><input type="name" name="project.email" size="50" value="${model.project.email}"/></td>
			<td>可选字段(多个，逗号分割)</td>
		</tr>
		<tr>
			<td>项目组号码</td>
			<td><input type="name" name="project.phone" size="50" value="${model.project.phone}"/></td>
			<td>可选字段(多个，逗号分割)</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><input class='btn btn-primary' type="submit" name="submit" value="submit" /></td>
		</tr>
	</table>
</form></div></div></div>

</a:body>