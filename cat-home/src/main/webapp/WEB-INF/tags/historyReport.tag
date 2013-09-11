<%@ tag trimDirectiveWhitespaces="true"  pageEncoding="UTF-8"%>
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
		var href = '<a href="?op=history&domain='+domain+'&date=${model.date}">&nbsp;[&nbsp;'+domain+'&nbsp;]&nbsp;</a>';
		return href;
	}
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="position">项目：<span class="text-error">${model.domain}</span>
			【<a href="javascript:showDomain()" id="switch">切换</a>】
			【<a href="javascript:showFrequent()" id="frequent">常用</a>】
			</td> 
			<td class="title"><span class="text-success"><span class="text-success"><span class="text-error">【报表时间】</span><jsp:invoke fragment="subtitle"/></span></td>
			</td>
			<td class="nav">
					<span class="text-error">【<a href="?domain=${model.domain}" class="switch"><span class="text-error">切到小时模式</span></a>】</span>
					&nbsp;&nbsp;<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq model.reportType}">
								&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]
						</c:when>
						<c:otherwise>
								&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=-1&${navUrlPrefix}">${model.currentNav.last}</a> ]
				&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=1&${navUrlPrefix}">${model.currentNav.next}</a> ]
				&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&reportType=${model.reportType}&nav=next&${navUrlPrefix}">now</a> ]
			</td>
		</tr>
	</table>
		<div class="domainNavbar" style="display:none;font-size:small">
			<table border="1" rules="all" >
				<c:forEach var="item" items="${model.domainGroups}">
					<tr>
						<c:set var="detail" value="${item.value}" />
						<td class="department" rowspan="${w:size(detail.projectLines)}">${item.key}</td>
						<c:forEach var="productline" items="${detail.projectLines}" varStatus="index">
								<c:if test="${index.index != 0}">
									<tr>
								</c:if>
								<td class="department">${productline.key}</td>
								<td><div class="domain"><c:forEach var="domain" items="${productline.value.lineDomains}">&nbsp;<c:choose><c:when test="${model.domain eq domain}"><a
														href="?op=history&domain=${domain}&date=${model.date}&reportType=${model.reportType}"
														class="current">[&nbsp;${domain}&nbsp;]</a></c:when>
														<c:otherwise><a
														href="?op=history&domain=${domain}&date=${model.date}&reportType=${model.reportType}">[&nbsp;${domain}&nbsp;]</a>
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
			<table border="1" rules="all">
				<tr>
					<td style="width:70px;" class="text-success">最近访问</td>
					<td style="word-break:break-all;" >
						<div class="domain" id="frequentNavbar">
						</div>
					</td>
				<tr>
			</table>
		</div>
		<jsp:doBody />
	</div>
</a:body>