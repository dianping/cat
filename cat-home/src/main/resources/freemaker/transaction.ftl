<html>
<body>
<br/>
<h3><a href="${current} target="_blank">TransactionReport</a></h3>
<table rules="all">
	<tr>
	</tr>
	<tr>
		<td></td>
		<td>Type</td>
		<td>Total Count</td>
		<td>Failure Count</td>
		<td>Failure%</td>
		<td>Avg(ms)</td>
		<td>TPS</td>
		<td>Link</td>
		<td></td>
	</tr>
	<#list types as item>
		<tr>
			<td></td>
			<td>${item.type.id}</td>
			<td>${item.type.totalCount}</td>
			<td>${item.type.failCount}</td>
			<td>${item.type.failPercent?string("percent")}</td>
			<td>${item.type.avg?string("0.00")}</td>
			<td>${item.type.tps?string("0.00")}</td>
			<td><a href="${item.url}">Graph</a></td>
		</tr>
	</#list>
	<tr></tr>
</table>
</body>
</html>

