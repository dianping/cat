<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#aggregationList').addClass('active');
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});
		});
	</script>
	<div>
		<div class="row-fluid">
        <div class="span2">
		<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			<div>
			</br>
			<table class="table table-striped table-bordered table-condensed" id="contents" width="100%">
			<thead>
				<tr class="odd">
					<th width="15%">报表类型</th>
					<th width="10%">域名</th>
					<th width="70%">规则</th>
					<!-- <th width="15%">显示名称</th>
					<th width="8%">示例</th> -->
					<th width="5%">操作&nbsp;&nbsp;  <a class='btn btn-primary btn-small' href="?op=aggregationUpdate">新增</a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.aggregationRules}"
					varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<c:choose>
							<c:when test="${item.type == 1}">
								<td>transaction</td>
							</c:when>
							<c:when test="${item.type == 2}">
								<td>event</td>
							</c:when>
							<c:when test="${item.type == 3}">
								<td>problem
							</c:when>
						</c:choose>
						<td>${item.domain}</td>
						<td>${item.pattern}</td><%-- 
						<td>${item.displayName}</td>
						<td>${item.sample}</td> --%>
						<td><a class='btn  btn-small btn-primary'href="?op=aggregationUpdate&pattern=${item.pattern}">编辑</a>
						<a class='delete btn  btn-small btn-danger' href="?op=aggregationDelete&pattern=${item.pattern}">删除</a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
		</div>
		</div></div></div>
</a:body>