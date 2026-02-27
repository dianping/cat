<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>
	
<a:config>
			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
			<thead>
				<tr >
					<th width="20%">项目组</th>
					<th width="20%">Type</th>
					<th width="20%">Name</th>
					<th width="20%">监控项</th>
					<th width="10%">是否告警</th>
					<th width="10%">操作 <a href="?op=transactionRuleUpdate" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.rules}" varStatus="status">
					<c:set var="conditions" value="${fn:split(item.id, ';')}" />
					<c:set var="domain" value="${conditions[0]}" />
					<c:set var="type" value="${conditions[1]}" />
					<c:set var="name" value="${conditions[2]}" />
					<c:set var="monitor" value="${conditions[3]}" />
					<tr class="">
						<td>${domain}</td>
						<td>${type}</td>
						<td>${name}</td>
						<td>${monitor}</td>
						<td>
                            <c:choose>
                                <c:when test="${item.available == false}">
                                    <span>否</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-danger">是</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
						<td><a href="?op=transactionRuleUpdate&ruleId=${item.id}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=transactionRuleDelete&ruleId=${item.id}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#alert_config').addClass('active open');
			$('#transactionRule').addClass('active');
 		});
	</script>
</a:config>
