<h4><a href="${current}" target="_blank">ProblemReport</a></h4>
<table rules="all" border="1" >
	<tr>
		<td>类型</td>
		<td>总量</td>
		<td>样本链接</td>
	</tr>
	<#list types as item>
		<tr>
			<td>${item.type}</td>
			<td style="text-align:right">${item.count}</td>
			<td style="text-align:right"><a href="${item.url}" target="_blank">图形</a></td>
		</tr>
	</#list>
</table>

