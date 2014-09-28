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
	<script type="text/javascript">
 	var commandChange = function commandChange() {
		var key = $("#command").val();
		var value = commandInfo[key];
		var code = document.getElementById("code");
		for ( var prop in value) {
			var opt = $('<option />');

			opt.html(value[prop].name);
			opt.val(value[prop].id);
			opt.appendTo(code);
		}
	}
		$(document).ready(function() {
			$('#appRule').addClass('active');
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
			<table class="table table-striped table-bordered table-condensed table-hover" id="contents" width="100%">
			<thead>
				<tr class="odd">
					<th width="15%">告警名</th>
					<th width="15%">命令字</th>
					<th width="10%">返回码</th>
					<th width="10%">网络类型</th>
					<th width="10%">版本</th>
					<th width="10%">连接类型</th>
					<th width="10%">平台</th>
					<th width="10%">地区</th>
					<th width="10%">运营商</th>
					<th width="10%">告警指标</th>
					<th width="5%">操作&nbsp;&nbsp;  <a class='btn btn-primary btn-small' href="?op=appRuleUpdate">新增</a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.rules}" varStatus="status">
					<c:set var="strs" value="${fn:split(item.id, ':')}" />
					<c:set var="conditions" value="${fn:split(strs[0], ';')}" />
					<c:set var="command" value="${conditions[0]}" />
					<c:set var="code" value="${conditions[1]}" />
					<c:set var="network" value="${conditions[2]}" />
					<c:set var="version" value="${conditions[3]}" />
					<c:set var="connectType" value="${conditions[4]}" />
					<c:set var="platform" value="${conditions[5]}" />
					<c:set var="city" value="${conditions[6]}" />
					<c:set var="operator" value="${conditions[7]}" />
					<c:set var="type" value="${strs[1]}" />
					<c:set var="name" value="${strs[2]}" />
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${name}</td>
						<c:choose>
							<c:when test="${command ne -1}">
							<td>
							<c:forEach var="i" items="${model.commands}">
							<c:if test="${i.id eq command}">${i.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${code ne -1}">
							<td>
							<c:forEach var="i" items="${model.command}">
								<c:if test="${i.key eq command}">
								<c:forEach var="i2" items="${i.value}">
									<c:if test="${i2.id eq code}">${i2.name}</c:if>
								</c:forEach>
								</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${network ne -1}">
							<td>
							<c:forEach var="i" items="${model.networks}">
							<c:if test="${i.value.id eq network}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${version ne -1}">
							<td>
							<c:forEach var="i" items="${model.versions}">
							<c:if test="${i.value.id eq version}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${connectType ne -1}">
							<td>
							<c:forEach var="i" items="${model.connectionTypes}">
							<c:if test="${i.value.id eq connectType}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${platform ne -1}">
							<td>
							<c:forEach var="i" items="${model.platforms}">
							<c:if test="${i.value.id eq platform}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${city ne -1}">
							<td>
							<c:forEach var="i" items="${model.cities}">
							<c:if test="${i.value.id eq city}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${operator ne -1}">
							<td>
							<c:forEach var="i" items="${model.operators}">
							<c:if test="${i.value.id eq operator}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						<td>
							<c:if test="${type eq 'request'}">请求数</c:if> 
							<c:if test="${type eq 'success'}">成功率</c:if>  
							<c:if test="${type eq 'delay'}">响应时间</c:if> 
						</td>
						<td><a class='btn  btn-small btn-primary'href="?op=appRuleUpdate&ruleId=${item.id}">编辑</a>
						<a class='delete btn  btn-small btn-danger' href="?op=appRuleDelete&ruleId=${item.id}">删除</a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
		</div>
		</div></div></div>
</a:body>