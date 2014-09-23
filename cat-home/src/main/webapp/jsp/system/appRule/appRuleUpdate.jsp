<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

${model.content}

<a:body>
	<div>
		<div class="row-fluid">
        <div class="span2">
		<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
		</br>
			<form name="appRuleUpdate" id="form" method="post" action="${model.pageUri}?op=appRuleSubmit">
				<table style='width:100%' class='table table-striped table-bordered'>
					<tr>
						<td>报表类型</td>
						<td><select id="reportType" name = "aggregation.type">	
						
						</select> </td>
					</tr>
					<tr>
						<td>域名</td>
						<td><input type="text" class="input-xlarge" value="FrontEnd" placeholder="聚合规则作用的域名" name="aggregation.domain" required value="${model.aggregationRule.domain}"/></td>
					</tr>
					<tr>
						<td>模板</td>
						<td><input type="text" class="input-xlarge"  placeholder="选择被聚合对象的模板" name="aggregation.pattern" required value="${model.aggregationRule.pattern}"/></td>
					</tr>
					<tr>
						<td>告警阈值</td>
						<td><input type="text" class="input-xlarge"  placeholder="告警阈值" name="aggregation.warn" required value="${model.aggregationRule.warn}"/></td>
					</tr>
					<tr>
						<td>联系邮件</td>
						<td><input type="text" class="input-xlarge"  placeholder="联系邮件" name="aggregation.mails" required value="${model.aggregationRule.mails}"/>（多个以逗号隔开）</td>
					</tr>
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