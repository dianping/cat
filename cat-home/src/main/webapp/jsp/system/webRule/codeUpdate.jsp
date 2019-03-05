<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.web.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.web.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.web.Model" scope="request"/>

<a:web_body>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_config').addClass('active open');
			$('#code').addClass('active');
		});
	</script>
	
	<h3 class="text-center text-success">返回码编辑</h3>
	<form name="codeSubmit" id="form" method="post" action="${model.pageUri}?op=codeSubmit">
	<table  style='width:60%;'  align="center" class="table table-striped table-condensed table-bordered ">
		<tr>
		<c:choose>
		<c:when test="${empty model.code}">
			<td>返回码</td><td><input name="code.id" value="${model.code.id}" id="codeId" required/><span class="text-danger">（* 仅支持数字）</span><br/></td>
		</c:when>
		<c:otherwise>
			<td>返回码</td><td><input name="code.id" value="${model.code.id}" id="codeId" readonly/><br/>
		</c:otherwise>
		</c:choose>
		<tr>
			<td>返回码说明</td><td><input name="code.name" value="${model.code.name}" id="codeName" required/><br/></td>
		</tr>
		<tr>
			<td>返回码状态</td><td>
				<select id="codeStatus" name="code.status">
				<c:choose>
				<c:when test="${model.code.status eq 1}">
					<option value='0' >成功</option>
					<option value='1' selected>失败</option>
				</c:when>
				<c:otherwise>
					<option value='0' >成功</option>
					<option value='1' >失败</option>
				</c:otherwise>
				</c:choose>
				</select><br/>
			</td>
		</tr>
		<tr>
			<td colspan="2" style="text-align:center;"><input class="btn btn-primary" type="submit" name="submit" value="提交" /></td>
		</tr>
	</table>
</form>
</a:web_body>
