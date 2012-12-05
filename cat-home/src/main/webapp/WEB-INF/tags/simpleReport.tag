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
			<td class="title">${title}</td>
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