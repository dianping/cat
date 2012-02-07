<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.ebay.com/webres"%>
<%@ attribute name="title"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="domain" fragment="true"%>
<%@ attribute name="nav" fragment="true"%>
<%@ attribute name="subtitle" fragment="true"%>

<a:body>

<res:useCss value='${res.css.local.report_css}' target="head-css" />

<div class="report">
	<table class="header">
		<tr>
			<td class="title">${title}</td>
			<td class="timestamp">Generated: ${timestamp}</td>
		</tr>
	</table>

	<table class="navbar">
		<tr>
			<td class="domain"><jsp:invoke fragment="domain"/></td>
			<td class="nav"><jsp:invoke fragment="nav"/></td>
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