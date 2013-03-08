<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.top.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.top.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.top.Model" scope="request"/>

<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.0/themes/base/jquery-ui.css" />
<script src="http://code.jquery.com/jquery-1.8.3.js"></script>
<script src="http://code.jquery.com/ui/1.10.0/jquery-ui.js"></script>
<a:body>
<res:useCss value='${res.css.local.report_css}' target="head-css" />
<res:useCss value='${res.css.local.table_css}' target="head-css" />

<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;Top Index Of Dianping</td>
		<td class="nav">
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
			</td>
	</table>
	
	<table width="100%">
		<tr><th>系统异常Top10（最近5分钟）</th></tr>
	</table>					
	<c:forEach var="item" items="${model.metrix.error.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>值</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${detail.domain}</td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
	
	<table width="100%">
		<tr><th>Url访问最慢Top10（最近5分钟）</th></tr>
	</table>					
	<c:forEach var="item" items="${model.metrix.url.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>值</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${detail.domain}</td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
	
	
	<table width="100%">
		<tr><th>Service访问最慢Top10（最近5分钟）</th></tr>
	</table>					
	<c:forEach var="item" items="${model.metrix.service.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>值</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${detail.domain}</td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
	
	<table width="100%">
		<tr><th>Call访问最慢Top10（最近5分钟）</th></tr>
	</table>					
	<c:forEach var="item" items="${model.metrix.call.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>值</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${detail.domain}</td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
	
	<table width="100%">
		<tr><th>SQL访问最慢Top10（最近5分钟）</th></tr>
	</table>					
	<c:forEach var="item" items="${model.metrix.sql.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>值</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${detail.domain}</td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
	
	<table width="100%">
		<tr><th>Cache访问最慢Top10（最近5分钟）</th></tr>
	</table>					
	<c:forEach var="item" items="${model.metrix.cache.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>值</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${detail.domain}</td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>

	<table class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
</a:body>