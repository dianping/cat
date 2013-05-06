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
				<td class="title">&nbsp;&nbsp;<jsp:invoke
						fragment="subtitle" /></td>
			<td class="nav"><c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a
						href="${model.baseUri}?date=${model.date}&ip=${model.ipAddress}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach> &nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
			</td>
		</table>

		<table class="navbar">
			<tr>
				<td class="domain">
					<div class="domain">
						<c:forEach var="domain" items="${model.domains}">
						&nbsp;<c:choose>
								<c:when test="${model.domain eq domain}">
									<a href="${model.baseUri}?domain=${domain}&ip=${model.ipAddress}&date=${model.date}"
										class="current">[&nbsp;${domain}&nbsp;]</a>
								</c:when>
								<c:otherwise>
									<a href="${model.baseUri}?domain=${domain}&ip=${model.ipAddress}&date=${model.date}">[&nbsp;${domain}&nbsp;]</a>
								</c:otherwise>
							</c:choose>&nbsp;
					</c:forEach>
					</div>
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