[CAT端到端数据报告] : [${yesterday}] [时间: ${date}]  <br/>
<table rules="all" border="1" >
	<tr>
		<td>分类</td>
		<td>公司</td>
		<td>命令字</td>
		<td>响应时间</td>
	</tr>
	<#list results as result>
			<#list result.items as item>
			<tr>
			 	<#if item_index == 0>
			   		<td rowspan=${result.size}>${result.id}</td>
			  	</#if>
				<td style="text-align:right">${item.id}</td>
				<td style="text-align:right">${item.command}</td>
				<td style="text-align:right">${item.delay}</td>
			</tr>
			</#list>
	</#list>
</table>
