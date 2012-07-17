<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ attribute name="title"%>
<%@ attribute name="navUrlPrefix"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="subtitle" fragment="true"%>

<a:body>

<res:useCss value='${res.css.local.report_css}' target="head-css" />

<div class="report">
	<table class="header">
		<tr>
			<td class="title">${title}&nbsp;&nbsp;<jsp:invoke fragment="subtitle"/></td>
			<td class="switch">Browse Mode:History Summarize Report 
				[&nbsp;<a href="?domain=${model.domain}">Hourly Mode</a>&nbsp;]&nbsp;
			</td>
		</tr>
	</table>
	<table class="navbar">
		<tr>
			<td class="domain" rowspan="2" style="vertical-align:top;">
				<div class="domain">
					<c:forEach var="domain" items="${model.domains}">
						&nbsp;<c:choose>
							<c:when test="${model.domain eq domain}">
								<a href="?op=history&domain=${domain}&date=${model.date}&reportType=${model.reportType}" class="current">[&nbsp;${domain}&nbsp;]</a>
							</c:when>
							<c:otherwise>
								<a href="?op=history&domain=${domain}&date=${model.date}&reportType=${model.reportType}">[&nbsp;${domain}&nbsp;]</a>
							</c:otherwise>
						</c:choose>&nbsp;
					</c:forEach>
				</div>
			</td>
			<td class="nav">
					&nbsp;&nbsp;[ <a href="?op=history&domain=${model.domain}&date=${model.date}&reportType=${model.reportType}&step=-1">${model.currentNav.last}</a> ]&nbsp;&nbsp;
					&nbsp;&nbsp;[ <a href="?op=history&domain=${model.domain}&date=${model.date}&reportType=${model.reportType}&step=1">${model.currentNav.next}</a> ]&nbsp;&nbsp;
					&nbsp;&nbsp;[ <a href="?op=history&domain=${model.domain}&reportType=${model.reportType}&nav=next">now</a> ]&nbsp;&nbsp;
			</td>
		</tr>
		<tr>
			<td class="nav">
				<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq model.reportType}">
								&nbsp;&nbsp;[ <a href="?op=history&domain=${model.domain}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]
						</c:when>
						<c:otherwise>
								&nbsp;&nbsp;[ <a href="?op=history&domain=${model.domain}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]&nbsp;&nbsp;
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;
			</td>
		</tr>
	</table>
	<jsp:doBody />

	<table class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>

</a:body>