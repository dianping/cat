<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ attribute name="title"%>
<%@ attribute name="navUrlPrefix"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="subtitle" fragment="true"%>

<a:body>

<res:useCss value='${res.css.local.report_css}' target="head-css" />
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>

<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;<jsp:invoke fragment="subtitle"/></td>
			<td class="switch"><a href="${model.baseUri}?op=history&domain=${model.domain}&ip=${model.ipAddress}">Switch To History Mode</a>
			</td>
			<td class="nav">
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&ip=${model.ipAddress}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
	<div class="position">	Current Domain:&nbsp;&nbsp;${model.department} &nbsp;&nbsp;>&nbsp;&nbsp;${model.projectLine} &nbsp;&nbsp;
	>&nbsp;&nbsp;${model.domain}&nbsp;&nbsp;
	&nbsp;&nbsp;[&nbsp;&nbsp;<a href="javascript:showDomain()" id="switch">More</a>&nbsp;&nbsp;]&nbsp;&nbsp;
			<script>
				function showDomain() {
					var b = $('#switch').html();
					if (b == 'More') {
						$('.navbar').slideDown();
						$('#switch').html("Less");
					} else {
						$('.navbar').slideUp();
						$('#switch').html("More");
					}
				}
			</script>
	</div> 
	<div class="navbar" style="display:none">
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
								<td><div class="domain"><c:forEach var="domain" items="${productline.value.lineDomains}">&nbsp;<c:choose><c:when test="${model.domain eq domain}"><a
														href="${model.baseUri}?domain=${domain}&date=${model.date}"
														class="current">[&nbsp;${domain}&nbsp;]</a></c:when>
														<c:otherwise><a
														href="${model.baseUri}?domain=${domain}&date=${model.date}">[&nbsp;${domain}&nbsp;]</a>
												</c:otherwise></c:choose>&nbsp;
										</c:forEach>
									</div>
								</td><c:if test="${index.index != 0}"></tr></c:if>
				</c:forEach></tr>
				</c:forEach>
			</table>
		</div>

	<jsp:doBody />

	<table class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>

</a:body>