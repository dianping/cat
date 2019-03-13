<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
		<h3 class="text-center text-success">编辑Crash告警规则</h3>
		<form name="crashRuleUpdate" id="form" method="post" action="${model.pageUri}?op=crashRuleUpdateSubmit">
			<table style='width:100%' class='table table-striped table-condensed '>
				<input type="hidden" class="input-xlarge"  name="crashRule.id" value="${model.crashRule.id}" />
				<tr>
					<td>AppId</td>
					<td>
					<c:choose>
					<c:when test="${fn:length(model.crashRule.id) eq 0}">
						<input type="text" class="input-xlarge" name="crashRule.appId" placeholder="AppId" value="${model.crashRule.appId}" required/>
					</c:when>
					<c:otherwise>
			  			<input type="text" class="input-xlarge" name="crashRule.appId"  value="${model.crashRule.appId}" readonly/>
					</c:otherwise>
					</c:choose>
					</td>
				</tr>
				<tr>
					<td>平台</td>
					<td>
					<c:choose>
					<c:when test="${fn:length(model.crashRule.id) eq 0}">
						<select id="platform" style="width:270px" name="crashRule.platform">
							<option value="android">android</option>
							<option value="ios">ios</option>
						</select>				
					</c:when>
					<c:otherwise>
		            	<input type="text" class="input-xlarge"  name="crashRule.platform" value="${model.crashRule.platform}" readonly/>
					</c:otherwise>
					</c:choose>
					</td>
				</tr>
				<tr>
					<td>模块</td>
					<td>
					<c:choose>
					<c:when test="${fn:length(model.crashRule.id) eq 0}">
						<input type="text" class="input-xlarge" name="crashRule.module" placeholder="模块" value="${model.crashRule.module}" required/>  &nbsp;如需对全部模块告警，请填ALL
					</c:when>
					<c:otherwise>
			  			<input type="text" class="input-xlarge" name="crashRule.module"  value="${model.crashRule.module}" readonly/>
					</c:otherwise>
					</c:choose>
					</td>
				</tr>
				<tr>
					<td>Warning阈值</td>
					<td><input type="text" class="input-xlarge"  placeholder="Warning阈值" name="crashRule.warnings" required value="${model.crashRule.warnings}"/> / 分钟</td>
				</tr>
				<tr>
					<td>Error阈值</td>
					<td><input type="text" class="input-xlarge"  placeholder="Error阈值" name="crashRule.errors" required value="${model.crashRule.errors}"/> / 分钟</td>
				</tr>
				<tr>
					<td>联系邮件</td>
					<td><input type="text" class="input-xlarge"  placeholder="联系邮件" name="crashRule.mails" required value="${model.crashRule.mails}"/>（多个以逗号隔开）</td>
				</tr>
				
				<tr>
					<td style='text-align:center' colspan='2'><input class='btn btn-primary btn-sm' type="submit" name="submit" value="提交" /></td>
				</tr>
			</table>
		</form>
		
<script type="text/javascript">
	$(document).ready(function() {
		$('#userMonitor_config').addClass('active open');
		$('#crashRule').addClass('active');
	});
</script> 
</a:mobile>