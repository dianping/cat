<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<jsp:useBean id="model" type="com.dianping.cat.report.page.browser.Model" scope="request"/>
<a:web_body>
<table class="table table-striped table-condensed table-bordered table-hover">

	<tr>
		<td width="15%">Error Time </td>
		<td width="85%">${model.jsErrorDetailInfo.errorTime}</td>
	</tr>
	<tr>
		<td>Level </td>
		<td>${model.jsErrorDetailInfo.level}</td>
	</tr>
	<tr>
		<td>Module </td>
		<td>${model.jsErrorDetailInfo.module}</td>
	</tr>
	<tr>
		<td>Dpid </td>
		<td>${model.jsErrorDetailInfo.dpid}</td>
	</tr>
	<tr>
		<td>Agent </td>
		<td>${model.jsErrorDetailInfo.agent}</td>
	</tr>
	<tr>
		<td>Detail </td>
		<td>${model.jsErrorDetailInfo.detail}</td>
	</tr>
</table>
<script type="text/javascript">
$(document).ready(
	function() {
		$('#Browser').addClass('active open');
		$('#web_problem').addClass('active');
});

</script>
</a:web_body>