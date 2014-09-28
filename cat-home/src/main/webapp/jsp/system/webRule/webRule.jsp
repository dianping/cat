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
		$(document).ready(function() {
			$('#webRule').addClass('active');
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
					<th width="10%">组</th>
					<th width="35%">URL</th>
					<th width="10%">省份</th>
					<th width="10%">城市</th>
					<th width="10%">运营商</th>
					<th width="10%">告警指标</th>
					<th width="5%">操作&nbsp;&nbsp;  <a class='btn btn-primary btn-small' href="?op=webRuleUpdate">新增</a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.rules}" varStatus="status">
					<c:set var="strs" value="${fn:split(item.id, ':')}" />
					<c:set var="conditions" value="${fn:split(strs[0], ';')}" />
					<c:set var="urlId" value="${conditions[0]}" />
					<c:set var="city" value="${fn:split(conditions[1], '-')}" />
					<c:set var="operator" value="${conditions[2]}" />
					<c:set var="type" value="${strs[1]}" />
					<c:set var="name" value="${strs[2]}" />
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${name}</td>
						<c:forEach var="i" items="${model.patternItems}">
							<c:if test="${i.name eq urlId}"><td>${i.group}</td><td>${i.pattern}</td></c:if>
						</c:forEach>
						<c:choose>
						<c:when test="${not empty city[0]}">
						<c:forEach var="i" items="${model.cityInfos}">
							<c:if test="${i.key eq city[0]}">
								<td>${i.key}</td>
								<c:choose>
								<c:when test="${not empty city[1]}">
								<c:forEach var="i2" items="${i.value}">
								<c:if test="${i2.city eq city[1]}">
								<td>${i2.city}</td>
								</c:if>
								</c:forEach>
								</c:when>
								<c:otherwise>
								<td>All</td>
								</c:otherwise>
								</c:choose>
							</c:if>
						</c:forEach>
						</c:when>
						<c:otherwise>
							<td>All</td>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						<c:choose>
						<c:when test="${not empty operator}">
							<td>${operator}</td>
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
						<td><a class='btn  btn-small btn-primary'href="?op=webRuleUpdate&ruleId=${item.id}">编辑</a>
						<a class='delete btn  btn-small btn-danger' href="?op=webRuleDelete&ruleId=${item.id}">删除</a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
		</div>
		</div></div></div>
</a:body>