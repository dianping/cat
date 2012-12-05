<h4><a href="${current}" target="_blank">ProblemReport</a></h4>
<table rules="all" border="1" >
	<tr>
		<td>Type</td>
		<td>Total Count</td>
		<td>Link</td>
	</tr>
	<#list types as item>
		<tr>
			<td>${item.type}</td>
			<td style="text-align:right">${item.count}</td>
			<td style="text-align:right"><a href="${item.url}" target="_blank">Graph</a></td>
		</tr>
	</#list>
</table>

