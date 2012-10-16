<h4><a href="${current}" target="_blank">ProblemReport</a></h4>
<table rules="all">
	<tr>
	</tr>
	<tr>
		<td></td>
		<td>Type</td>
		<td>Total Count</td>
		<td>Link</td>
		<td></td>
	</tr>
	<#list types as item>
		<tr>
			<td></td>
			<td>${item.type}</td>
			<td style="text-align:right">${item.count}</td>
			<td style="text-align:right"><a href="${item.url}" target="_blank">Graph</a></td>
		</tr>
	</#list>
	<tr></tr>
</table>

