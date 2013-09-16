<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ attribute name="title"%>
<%@ attribute name="navUrlPrefix"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="subtitle" fragment="true"%>

<a:body>
<script>
	function buildHref(domain){
		var href = '<a href="?domain='+domain+'&date=${model.date}">&nbsp;[&nbsp;'+domain+'&nbsp;]&nbsp;</a>';
		return href;
	}
	$(document).ready(function() {
		var domains= getcookie('CAT_DOMAINS');
		var domainArray =domains.split("|");
		var html = '';
		var length =domainArray.length;
		
		for(var i=0;i<length;i++){
			var href = buildHref(domainArray[i])
			html+= href;
		}
		$('#frequentNavbar').html(html);
	});
</script>

<div class="report">
	<table class="header">
		<tr>
			<td class="position">项目：<span class="text-error">${model.domain}</span>
			【<a href="javascript:showDomain()" id="switch">切换</a>】
			【<a href="javascript:showFrequent()" id="frequent">常用</a>】
			</td> 
			<td class="title"><span class="text-success"><span class="text-error">【报表时间】</span><jsp:invoke fragment="subtitle"/></span></td>
			<td class="nav">
				<span class="text-error">【<a class="switch" href="${model.baseUri}?op=history&domain=${model.domain}&ip=${model.ipAddress}"><span class="text-error">切到历史模式</span></a>】</span>
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&ip=${model.ipAddress}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
	
	<div class="domainNavbar" style="display:none;">
		<table border="1" rules="all">
			<c:forEach var="item" items="${model.domainGroups}">
				<tr>
					<c:set var="detail" value="${item.value}" />
					<td class="department" rowspan="${w:size(detail.projectLines)}">${item.key}</td>
					<c:forEach var="productline" items="${detail.projectLines}" varStatus="index">
							<c:if test="${index.index != 0}">
								<tr>
							</c:if>
							<td class="department">${productline.key}</td>
							<td><div class="domain">
								<c:forEach var="domain" items="${productline.value.lineDomains}">&nbsp;<c:choose><c:when test="${model.domain eq domain}"><a
											href="${model.baseUri}?domain=${domain}&date=${model.date}"
											class="current">[&nbsp;${domain}&nbsp;]</a></c:when>
											<c:otherwise><a
											href="${model.baseUri}?domain=${domain}&date=${model.date}">[&nbsp;${domain}&nbsp;]</a>
									</c:otherwise></c:choose>&nbsp;
								</c:forEach>
							</div>
							</td><c:if test="${index.index != 0}"></tr></c:if>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
	<div class="frequentNavbar" style="display:none;font-size:small">
		<table class="table" border="1" rules="all">
			<tr>
				<td style="width:70px;" class="text-success">最近访问</td>
				<td class="domain"  style="word-break:break-all" id="frequentNavbar"></td>
			<tr>
		</table>
	</div>
	<jsp:doBody />
</div>

</a:body>