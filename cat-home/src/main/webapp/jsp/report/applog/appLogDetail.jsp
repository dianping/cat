<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<jsp:useBean id="model" type="com.dianping.cat.report.page.applog.Model" scope="request"/>
<a:mobile>
<table class="table table-striped table-condensed table-bordered table-hover">

	<tr>
		<td width="15%">Log Time </td>
		<td width="85%">${model.appLogDetailInfo.logTime}</td>
	</tr>
	<tr>
		<td>APP</td>
		<td>${model.appLogDetailInfo.appName} - ${model.appLogDetailInfo.appVersion}</td>
	</tr>
	<tr>
		<td>Platform</td>
		<td>${model.appLogDetailInfo.platform} - ${model.appLogDetailInfo.platformVersion}</td>
	</tr>
	<tr>
		<td>Level </td>
		<td>${model.appLogDetailInfo.level}</td>
	</tr>
	<tr>
		<td>Device </td>
		<td>${model.appLogDetailInfo.deviceBrand} - ${model.appLogDetailInfo.deviceModel}</td>
	</tr>
	<tr>
		<td>UnionId </td>
		<td>${model.appLogDetailInfo.unionId}</td>
	</tr>
	<tr>
		<td>Detail </td>
		<td>${model.appLogDetailInfo.detail}</td>
	</tr>
</table>
<script type="text/javascript">
$(document).ready(
	function() {
		$('#App_report').addClass("active open");
		$('#appLog').addClass('active');
});

</script>
</a:mobile>