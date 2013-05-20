<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.aggregation.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.aggregation.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.aggregation.Model" scope="request"/>


<a:body>
	<res:useCss value='${res.css.local.alarm_css}' target="head-css" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
<form name="aggregationUpdate" id="form" method="post" action="${model.pageUri}?op=updateSubmit">
	<table>
		<tr><td><input type="hidden" name="id" value="${model.aggregationRule.id}" /></td></tr>
		<tr>
			<td>报表类型</td>
			<td><select id="reportType" name = "type">	
				<c:choose>
					<c:when test="${model.aggregationRule.type == 1}">
						<option value="1"  selected="selected">transaction</option>
						<option value="2">event</option>
						<option value="3">problem</option>
					</c:when>
					<c:when test="${model.aggregationRule.type == 2}">
						<option value="1">transaction</option>
						<option value="2" selected="selected">event</option>
						<option value="3">problem</option>
					</c:when>
					<c:when test="${model.aggregationRule.type == 3}">
						<option value="1">transaction</option>
						<option value="2">event</option>
						<option value="3" selected="selected">problem</option>
					</c:when>
					<c:otherwise>
						<option value="1"  selected="selected">transaction</option>
						<option value="2">event</option>
						<option value="3">problem</option>
					</c:otherwise>
				</c:choose>
			</select> </td>
			<!-- td><input type="text" name="type" value="${model.aggregationRule.type}"/></td> -->
			<td style='color:red'>聚合规则作用的报表类型</td>
		</tr>
		<tr>
			<td>域名</td>
			<td><input type="text" name="domain" value="${model.aggregationRule.domain}"/></td>
			<td style='color:red'>聚合规则作用的域名</td>
		</tr>
		<tr>
			<td>模板</td>
			<td><input type="text" name="pattern" value="${model.aggregationRule.pattern}"/></td>
			<td style='color:red'>选择被聚合对象的模板</td>
		</tr>
		<tr>
			<td>显示名称</td>
			<td><input type="text" name="display_name" value="${model.aggregationRule.displayName}"/></td>
			<td style='color:red'>聚合显示的名称</td>
		</tr>
		<tr>
			<td>示例</td>
			<td><input type="text" name="sample" value="${model.aggregationRule.sample}"/></td>
			<td>被聚合对象的示例</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><input type="submit" name="submit" value="submit" /></td>
		</tr>
	</table>
</form>
</a:body>