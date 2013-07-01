<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<div>
		<div class="row-fluid">
        <div class="span2">
		<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
		</br>
			<form name="aggregationUpdate" id="form" method="post" action="${model.pageUri}?op=aggregationUpdateSubmit">
				<table style='width:60%' class='table table-striped table-bordered'>
					<tr>
						<td>报表类型</td>
						<td><select id="reportType" name = "aggregation.type">	
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
									<option value="1">transaction</option>
									<option value="2">event</option>
									<option value="3" selected="selected">problem</option>
								</c:otherwise>
							</c:choose>
						</select> </td>
						<!-- td><input type="text" name="type" value="${model.aggregationRule.type}"/></td> -->
					</tr>
					<tr>
						<td>域名</td>
						<td><input type="text" class="input-xlarge" value="FrontEnd" placeholder="聚合规则作用的域名" name="aggregation.domain" required value="${model.aggregationRule.domain}"/></td>
					</tr>
					<tr>
						<td>模板</td>
						<td><input type="text" class="input-xlarge"  placeholder="选择被聚合对象的模板" name="aggregation.pattern" required value="${model.aggregationRule.pattern}"/></td>
					</tr>
					<%-- <tr>
						<td>显示名称</td>
						<td><input type="text" class="input-xlarge" placeholder="聚合显示的名称" name="aggregation.displayName" required value="${model.aggregationRule.displayName}"/></td>
					</tr>
					<tr>
						<td>示例</td>
						<td><input type="text" class="input-xlarge" placeholder="被聚合对象的示例" name="aggregation.sample" required value="${model.aggregationRule.sample}"/></td>
					</tr> --%>
					<tr>
						<td style='text-align:center' colspan='2'><input class='btn btn-primary' type="submit" name="submit" value="submit" /></td>
					</tr>
				</table>
			</form> </div></div></div>
</a:body>
<script type="text/javascript">
	$(document).ready(function() {
		$('#aggregationList').addClass('active');
	});
</script>