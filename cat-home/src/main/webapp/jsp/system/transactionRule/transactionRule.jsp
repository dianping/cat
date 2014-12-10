<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
			<div>
			</br>
			<table class="table table-striped table-bordered table-condensed table-hover" id="contents" width="100%">
			<thead>
				<tr class="odd">
					<th width="15%">项目组</th>
					<th width="30%">Type</th>
					<th width="30%">Name</th>
					<th width="15%">操作&nbsp;&nbsp;  <a class='btn btn-primary btn-sm' href="?op=transactionRuleUpdate">新增</a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.rules}" varStatus="status">
					<c:set var="conditions" value="${fn:split(item.id, ';')}" />
					<c:set var="domain" value="${conditions[0]}" />
					<c:set var="type" value="${conditions[1]}" />
					<c:set var="name" value="${conditions[2]}" />
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${domain}</td>
						<td>${type}</td>
						<td>${name}</td>
						<td><a class='btn  btn-sm btn-primary'href="?op=transactionRuleUpdate&ruleId=${item.id}">编辑</a>
						<a class='delete btn  btn-sm btn-danger' href="?op=transactionRuleDelete&ruleId=${item.id}">删除</a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
		</div>
		</div></div></div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#transactionRule').addClass('active');
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});
 		});
	</script>
</a:body>