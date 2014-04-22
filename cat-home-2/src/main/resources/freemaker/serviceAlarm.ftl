<h4>服务调用失败告警</h4>
<table rules="all" border="1" >
	<tr>
		<td>项目名称</td>
		<td>${domain}</td>
	</tr>
	<tr>
		<td>告警时间</td>
		<td>${date?string("yyyy-MM-dd HH:mm:ss")}</td>
	</tr>
	<tr>
		<td>告警规则</td>
		<td>${rule}</td>
	</tr>
	<tr>
		<td>错误个数</td>
		<td>${count}</td>
	</tr>
	<tr>
		<td>CAT链接</td>
		<td> <a href="${url}" target="_blank">link</a></td>
	</tr>
</table>