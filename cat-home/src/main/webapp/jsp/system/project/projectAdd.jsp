<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />

	<script type="text/javascript">
		$(document).ready(function() {
			$('#domain-config').addClass('active open');
			$('#projects').addClass('active');
		});
	</script>
	<div style="padding:5px;">
	<form name="projectUpdate" id="form" method="get" action="${model.pageUri}?op=updateSubmit">
	<table class="table table-striped table-condensed ">
		<input type="hidden" name="op" value="updateSubmit" />
		<tr>
			<td style="width:10%;">应用名称</td>
			<td><input type="name" class="form-control" name="project.domain" /></td>
			<td style="color:red;width:60%">注意：建议使用半角英文和半角符号(. -)。</td>
		</tr>
        <tr style="display: none">
			<td style="width:15%;">CMDB应用名称</td>
			<td><input type="name" class="form-control" name="project.cmdbDomain" value="default" /></td>
			<td>CMDB中项目统一名称<span  style="color:red">【CMDB中没有的话，与CAT上的应用名称保持一致即可】</span></td>
		</tr>
        <tr style="display: none">
			<td style="width:15%;">CMDB项目级别</td>
			<td><input type="name" class="form-control" name="project.level" value="1" /></td>
			<td>CMDB中项目统一级别<span  style="color:red">【此字段会和CMDB信息同步】</span></td>
		</tr>
		<tr>
			<td style="width:15%;">事业部</td>
			<td><input type="name" class="form-control" name="project.bu" /></td>
			<td>所属部门名称</td>
		</tr>
		<tr>
			<td style="width:15%;">产品线</td>
			<td><input type="name" class="form-control" name="project.cmdbProductline" /></td>
            <td>所属产品线名称</td>
		</tr>
		<tr>
			<td style="width:15%;">负责人</td>
			<td><input type="name" class="form-control" name="project.owner" /></td>
			<td>项目负责人姓名</td>
		</tr>
		<tr>
			<td style="width:15%;">项目组邮件</td>
			<td><input type="name" class="form-control" name="project.email" /></td>
			<td>字段(多个，逗号分割)</td>
		</tr>
		<tr>
			<td style="width:15%;">项目组号码</td>
			<td><input type="name" class="form-control" name="project.phone" /></td>
			<td>字段(多个，逗号分割)</td>
		</tr>
		<tr>
			<td style="width:15%;">项目键</td>
			<td><input type="name" class="form-control" name="project.key" /></td>
			<td>系统代码</td>
		</tr>
		<tr>
			<td style="width:15%;">项目经办人</td>
			<td><input type="name" class="form-control" name="project.assigner" /></td>
			<td>系统默认分配账号</td>
		</tr>
		<tr>
			<td colspan="2" align="center"><input class='btn btn-primary btn-sm' type="submit" name="submit" value="提交" />&nbsp;
		</tr>
	</table>
</form>

</div>
</a:config>
<style>
.input-icon>.ace-icon {
	z-index: 0;
}
</style>
