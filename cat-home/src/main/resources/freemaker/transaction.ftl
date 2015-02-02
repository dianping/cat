<h4><a href="${current}" target="_blank">TransactionReport</a></h4>
<table rules="all" border="1" >
	<tr>
		<td>Type</td>
		<td>Total Count</td>
		<td>Failure Count</td>
		<td>Failure%</td>
		<td>Avg(ms)</td>
		<td>TPS</td>
		<td>Link</td>
	</tr>
	<#list types as item>
		<tr>
			<td>${item.type.id}</td>
			<td style="text-align:right">${item.type.totalCount}</td>
			<td style="text-align:right">${item.type.failCount}</td>
			<td style="text-align:right">${item.type.failPercent?string("0.00")}</td>
			<td style="text-align:right">${item.type.avg?string("0.00")}</td>
			<td style="text-align:right">${item.type.tps?string("0.00")}</td>
			<td style="text-align:right"><a href="${item.url}" target="_blank">Graph</a></td>
		</tr>
	</#list>
</table>

