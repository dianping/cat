<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model" scope="request" />
<script src="http://code.jquery.com/jquery-1.8.3.js"></script>
	
<a:body>
	<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
	<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
	<res:useCss value="${res.css.local['bootstrap-rowlink.css']}" target="head-css" />
	<res:useJs value="${res.js.local['bootstrap-rowlink.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['abtestAllTest.js']}" target="head-js" />
	
	<style>
	.statusSpan {
		float: right;
		margin-right: 0.5em;
		padding-right: 5px;
		padding-left: 5px;
	}
	
	.selected	{
		background-color: #D8DFEA;
		border-radius: 5px;
		font-weight: bold;
		padding-left: 4px;
	}
	
	.liHover > li {
		line-height: 1.3em;
	}
	
	input.search-query {
		-webkit-border-radius: 4px;
		-moz-border-radius: 4px;
		border-radius: 4px;
	}
	
	#search-submit {
		position: absolute;
		top: 6px;
		right: 10px;
		display: inline-block;
		width: 14px;
		height: 14px;
		*margin-right: .3em;
		line-height: 14px;
		text-indent: -9999px;
		vertical-align: text-top;
		cursor: pointer;
		background-color: transparent;
		background-image: url("${model.webapp}/img/glyphicons-halflings.png");
		background-position: -48px 0;
		background-repeat: no-repeat;
		border: 0 none;
		opacity: 0.75;
	}
	
	tr.middle > td {
		vertical-align: middle;
		padding-bottom: 0;
	}
	
	tr.center > td{
		text-align: center;
	}
	
	tr.centerth > th{
		text-align: center;
	}
	
	</style>
	<br>
	<div class="row-fluid clearfix">
		<div class="span2 column">
			<form class="navbar-search" action="">
				<input name="q" id="search" class="search-query"
					placeholder="Search..."> <input type="submit"
					value="Search" id="search-submit">
			</form>
			<div style="margin-top: 40px;">
				<ul class="nav nav-list well liHover">
					<li class="nav-header">ABTest Status</li>
					<li class="divider" />
					<li${payload.status eq 'created' ? ' class="selected"' : ''}>
						<a href="?op=list&status=created">
						<img height="12" width="12" src="${res.img.local['CREATED_black_small.png']}"> created
						<span class="badge statusSpan">${model.createdCount}</span>
						</a>
					</li>
					<li${payload.status eq 'ready' ? ' class="selected"' : ''}>
						<a href="?op=list&status=ready">
						<img height="12" width="12" src="${res.img.local['READY_black_small.png']}"> ready to start
						<span class="badge statusSpan">${model.readyCount}</span>
						</a>
					</li>
					<li${payload.status eq 'running' ? ' class="selected"' : ''}>
						<a href="?op=list&status=running">
						<img height="12" width="12" src="${res.img.local['RUNNING_black_small.png']}"> running
						<span class="badge statusSpan">${model.runningCount}</span>
						</a>
					</li>
					<li${payload.status eq 'terminated' ? ' class="selected"' : ''}>
						<a href="?op=list&status=terminated">
						<img height="12" width="12" src="${res.img.local['STOPPED_black_small.png']}"> terminated
						<span class="badge statusSpan">${model.terminatedCount}</span>
						</a>
					</li>
					<li${payload.status eq 'suspended' ? ' class="selected"' : ''}>
						<a href="?op=list&status=suspended">
						<img height="12" width="12" src="${res.img.local['PAUSED_black_small.png']}"> suspended
						<span class="badge statusSpan">${model.suspendedCount}</span>
						</a>
					</li>
				</ul>
			</div>
		</div>
		<div class="span10 column">
			<div style="margin-bottom: 10px;">
				<button class="btn" type="button">
					<label class="checkbox"> <input id="ckall" type="checkbox"></input></label>
				</button>
				<button id="btnSuspend" class="btn" type="button">Suspend</button>
				<button id="btnResume" class="btn" type="button">Resume</button>
			</div>

			<table class="table table-striped table-format table-hover" data-provides="rowlink">
				<thead>
					<tr class="centerth">
						<th width="1%"></th>
						<th width="6%">ID</th>
						<th>Name</th>
						<th>PV</th>
						<th>ConversionRate</th>
						<th>Status</th>
						<th>Created On</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="item" items="${model.reports}">
						<tr class="middle center rowlink">
							<td class="nolink" style="padding-bottom: 8px"><input type="checkbox"/></td>
							<td><a href="abtest?op=report&id=${item.entity.id}">${item.entity.id}</a></td>
							<td>${item.entity.name}</td>
							<td>1000</td>
							<td>10%</td>
							<td>
								<c:choose>
									<c:when test="${item.status.status eq 'created'}">
										<div>
											<img src="${res.img.local['CREATED_colored_big.png']}" />
										</div> <small>Created</small>
									</c:when>
									<c:when test="${item.status.status eq 'running'}">
										<div>
											<img src="${res.img.local['RUNNING_colored_big.png']}" />
										</div> <small>Running</small>
									</c:when>
									<c:when test="${item.status.status eq 'terminated'}">
										<div>
											<img src="${res.img.local['STOPPED_colored_big.png']}">
										</div> <small>Terminated</small>
									</c:when>
									<c:when test="${item.status.status eq 'ready'}">
										<div>
											<img src="${res.img.local['READY_colored_big.png']}">
										</div> <small>Ready to start</small>
									</c:when>
									<c:when test="${item.status.status eq 'suspended'}">
										<div>
											<img src="${res.img.local['PAUSED_colored_big.png']}">
										</div> <small>Suspended</small>
									</c:when>
								</c:choose> 
							</td>
							<td>Apr 1, 2013</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>

			<div style="margin-top: 0px">  
			  <ul class="pager">  
			  	<li ${payload.pageNum <= 1 ? ' class="disabled"' : ''}>
			  		<a href="?op=list&status=${payload.status}&pageNum=1">First</a>
			  	</li>
			    <li id="prev" ${payload.pageNum <= 1 ? ' class="disabled"' : ''}>
			    	<a href="?op=list&status=${payload.status}&pageNum=${payload.pageNum > 1 ? (payload.pageNum - 1) : 1}">&larr; Prev</a>
			    </li>
					<fmt:parseNumber var="beginPage" integerOnly="true" value="${payload.pageNum / 5}" />
					<fmt:parseNumber var="pageRemainder" integerOnly="true" value="${payload.pageNum % 5}" />
					<c:if test="${pageRemainder == 0}">
						<fmt:parseNumber var="beginPage" integerOnly="true" value="${beginPage - 1}" />
					</c:if>
					<c:forEach var="pageNum" step="1"
						begin="${(beginPage * 5 + 1)<= model.totalPages ? (beginPage * 5 + 1) : model.totalPages }"
						end="${(beginPage + 1) * 5 <= model.totalPages ? (beginPage + 1) * 5 : model.totalPages}">
						<li ${payload.pageNum == pageNum ? ' class="disabled"' : ''}>
							<a href="?op=list&status=${payload.status}&pageNum=${pageNum}">${pageNum}</a>
						</li>
					</c:forEach>
					<li id="next" ${payload.pageNum >= model.totalPages ? ' class="disabled"' : ''}>
			    	<a href="?op=list&status=${payload.status}&pageNum=${payload.pageNum < model.totalPages ? (payload.pageNum + 1) : model.totalPages}">Next &rarr;</a>
			    </li>  
			    <li ${payload.pageNum >= model.totalPages ? ' class="disabled"' : ''}>
			    	<a href="?op=list&status=${payload.status}&pageNum=${model.totalPages}">Last</a>
			    </li>
			  </ul>  
			</div>
		</div>
	</div>
</a:body>