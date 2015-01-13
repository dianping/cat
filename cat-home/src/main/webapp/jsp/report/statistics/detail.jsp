<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<style>
.dataTables_wrapper {
	position: absolute;
}
</style>
<table class="table table-striped table-condensed table-hover" style="width:100%" id="contents">
	<thead>
	<tr>
		<th width="20%">项目</th>
		<c:forEach var="key" items="${model.keys}" varStatus="status">
			<th>${key}[机器数]</th>
			<th>${key}[高峰]</th>
			<th>${key}[全天]</th>
		</c:forEach>
	</tr>
	</thead>
	<tbody>
	<c:forEach var="entry" items="${model.systemReport.domains}" varStatus="status">
		<tr>
			<td>${entry.key}</td>
			<c:forEach var="key" items="${model.keys}">
				<td>${fn:length(model.systemReport.domains[entry.key].entities[key].machines)}</td>
				<td>${w:format(model.systemReport.domains[entry.key].entities[key].rush.avg,'###,##0.00')}</td>
				<td>${w:format(model.systemReport.domains[entry.key].entities[key].day.avg,'###,##0.00')}</td>
			</c:forEach>
		</tr>
	</c:forEach>
	</tbody>
</table>
