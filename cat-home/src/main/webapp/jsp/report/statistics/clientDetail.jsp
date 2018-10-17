<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<style>
.dataTables_wrapper {
	position: absolute;
}
</style>
<table class="table table-striped table-condensed table-hover" style="width:100%" id="contents">
	<thead>
		<th>项目</th>
		<th>接口</th>
		<th width="8%">总量</th>
		<th width="5%">失败</th>
		<th width="8%">失败率</th>
		<th width="8%">响应时间</th>
		<th width="8%">超时时间</th>
	</thead>
	<tbody>
	<c:forEach var="entry" items="${model.clientReport.domains}" varStatus="status">
		<c:forEach var="method" items="${entry.value.methods}">
		<tr>
			<td>${entry.key}</td>
			<td>${method.value.id}</td>
			<td>${w:format(method.value.totalCount,'##0')}</td>
			<td>${w:format(method.value.failureCount,'##0')}</td>
			<td>${w:format(method.value.failurePercent,'##0.0000')}</td>
			<td>${w:format(method.value.avg,'##0.00')}</td>
			<td>${w:format(method.value.timeout,'##0.00')}</td>
			</tr>
		</c:forEach>
	</c:forEach>
	</tbody>
</table>
