<h4><a href="${current}" target="_blank">TransactionReport</a></h4>
<table rules="all" border="1" >
	<tr>
		<td>类型</td>
		<td>总量</td>
		<td>失败次数</td>
		<td>错误率</td>
		<td>平均耗时(ms)/td>
		<td>TPS</td>
		<td>样本链接</td>
	</tr>
	<#list types as item>
		<tr>
			<td>${item.type.id}</td>
			<td style="text-align:right">${item.type.totalCount}</td>
			<td style="text-align:right">${item.type.failCount}</td>
			<td style="text-align:right">${item.type.failPercent?string("0.00")}</td>
			<td style="text-align:right">${item.type.avg?string("0.00")}</td>
			<td style="text-align:right">${item.type.tps?string("0.00")}</td>
			<td style="text-align:right"><a href="${item.url}" target="_blank">时序图</a></td>
		</tr>
	</#list>
</table>

