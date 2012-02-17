<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ attribute name="title"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="subtitle" fragment="true"%>

<a:body>

<res:useCss value='${res.css.local.report_css}' target="head-css" />

<div class="report">
	<table class="header">
		<tr>
			<td class="title">${title}  &nbsp;${model.reportTitle}</td>
			<td class="timestamp">Generated: ${model.generateTime}</td>
		</tr>
	</table>

	<table class="navbar">
		<tr>
			<td class="domain">
				<div class="domain">
					<c:forEach var="domain" items="${model.domains}">
						&nbsp;[<c:choose>
							<c:when test="${model.currentDomain eq domain}">
								<a href="?domain=${domain}" class="current">&nbsp;${domain}&nbsp;</a>
							</c:when>
							<c:otherwise>
								<a href="?domain=${domain}">&nbsp;${domain}&nbsp;</a>
							</c:otherwise>
						</c:choose>	]&nbsp;
					</c:forEach>
				</div>
			</td>
			<td class="nav">
				<c:forEach var="url" items="${model.urlNavs}">
					&nbsp;[ <a href="${model.urlPrefix}${url.method}">${url.text}</a> ]&nbsp;
				</c:forEach>
			</td>
		</tr>
		<tr>
			<td class="subtitle"><jsp:invoke fragment="subtitle"/></td>
		</tr>
	</table>
	<br />

	<jsp:doBody />

	<br />
	<table class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>

</a:body>