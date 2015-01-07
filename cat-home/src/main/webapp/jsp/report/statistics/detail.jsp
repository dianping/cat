<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<style>
	.tableHeader:hover {
		cursor:hand;
	}
</style>
<div class="tabbable "  style="padding-top:3px;"> <!-- Only required for left/right tabs -->
	<div class="tab-content">
				<table	class="problem table table-striped table-condensed table-hover">
					<thead>
					<tr class="text-success">
						<th width="15%">项目</th>
						<th width="20%">区间</th>
						<c:forEach var="key" items="${model.keys}" varStatus="status">
							<th>${key}</th>
						</c:forEach>
					</tr>
					</thead>
					<tbody>
					<c:forEach var="entry" items="${model.systemReport.domains}" varStatus="status">
						<tr>
							<td rowspan="2">${entry.key}</td>
							<td>高峰平均值（16:00-18:00）</td>
							<c:forEach var="key" items="${model.keys}">
								<td>${w:format(model.systemReport.domains[entry.key].entities[key].rush.avg,'###,##0.00')}</td>
							</c:forEach>
							<tr>
							<td>全天平均值</td>
							<c:forEach var="key" items="${model.keys}">
								<td>${w:format(model.systemReport.domains[entry.key].entities[key].day.avg,'###,##0.00')}</td>
							</c:forEach>
							</tr>
						</tr>
					</c:forEach>
					</tbody>
				</table>
	</div>
</div>
