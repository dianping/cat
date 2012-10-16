<h4><a href="${current}" target="_blank">EventReport</a></h4>
<table rules="all">
	<tr>
	</tr>
	<tr>
		<td></td>
		<td>Type</td>
		<td>Total Count</td>
		<td>Failure Count</td>
		<td>Failure%</td>
		<td>TPS</td>
		<td>Link</td>
		<td></td>
	</tr>
	<#list types as item>
		<tr>
			<td></td>
			<td>${item.type.id}</td>
			<td style="text-align:right">${item.type.totalCount}</td>
			<td style="text-align:right">${item.type.failCount}</td>
			<td style="text-align:right">${item.type.failPercent?string("0.00")}</td>
			<td style="text-align:right">${item.type.tps?string("0.00")}</td>
			<td style="text-align:right"><a href="${item.url}" target="_blank">Graph</a></td>
			<td></td>
		</tr>
	</#list>
	<tr></tr>
</table>

