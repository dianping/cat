<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form name="exceptionConfig" id="form" method="post"
	action="${model.pageUri}?op=exceptionThresholdUpdateSubmit">
	<h4 class="text-center text-error" id="state">&nbsp;</h4>
	<h4 class="text-center text-error">修改异常报警配置信息</h4>
	<table class="table table-striped table-bordered table-condensed">
		<tr>
			<td width="40%" style="text-align: right" class="text-success">域名</td>
			<td> <input id="domain" name="exceptionLimit.domain"
				value="${model.exceptionLimit.domain}" required 
				<c:if test="${model.exceptionLimit!= null}">
   					readonly
				</c:if>
				/></td>
		</tr>
	
		<tr>
			<td style="text-align: right" class="text-success">异常名称</td>
			<td><input id="exceptionName" name="exceptionLimit.id"
				value="${model.exceptionLimit.id}" required 
				<c:if test="${model.exceptionLimit!= null}">
   					readonly
				</c:if>
				/></td>
		</tr>
		
		<tr>
			<td style="text-align: right" class="text-success">warning阈值</td>
			<td><input id="warningThreshold" name="exceptionLimit.warning"
				value="${model.exceptionLimit.warning}" required /></td>
		</tr>
		
		<tr>
			<td style="text-align: right" class="text-success">error阈值</td>
			<td><input id="errorThreshold" name="exceptionLimit.error"
				value="${model.exceptionLimit.error}" required /></td>
		</tr>

		<tr>
			<td>&nbsp;</td>
			<td><input class='btn btn-primary' id="addOrUpdateExceptionConfigSubmit" type="submit"
				name="submit" value="提交"/></td>
		</tr>
	</table>
</form>