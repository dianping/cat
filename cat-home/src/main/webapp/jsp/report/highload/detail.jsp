<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<br/>
<div class="tabbable "  > <!-- Only required for left/right tabs -->
	<ul class="nav nav-tabs">
		<c:forEach var="reportEntry" items="${model.reports}" varStatus="status">
			<li class="text-right navTabs"><a href="#${reportEntry.key}" data-toggle="tab"><strong>${reportEntry.key}</strong></a></li>
		</c:forEach>
	</ul>
	<div class="tab-content">
		<c:forEach var="reportEntry" items="${model.reports}" varStatus="status">
			<div class="tab-pane" id="${reportEntry.key}">
				<table	class="problem table table-striped table-bordered table-condensed table-hover">
					<thead>
					<tr class="text-success">
						<th width="8%">domain</th>
						<th width="20%">Name</th>
						<th width="5%">总数</th>
						<th width="5%">错误数</th>
						<th width="7%">失败率</th>
						<th width="4%">Min</th>
						<th width="5%">Max</th>
						<th width="5%">Avg</th>
						<th width="5%">95Line</th>
						<th width="6%">99.9Line</th>
						<th width="5%">Std</th>
						<th width="5%">QPS</th>
					</tr>
					</thead>
					<tbody>
					<c:forEach var="report" items="${reportEntry.value}" varStatus="status">
						<c:set var="e" value="${report.name}"/>
						<tr>
							<td>${report.domain}</td>
							<td>${e.id}</td>
							<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
							<td>${w:format(e.failCount,'#,###,###,###,##0')}</td>
							<td>&nbsp;${w:format(e.failPercent/100,'0.0000%')}</td>
							<td>${w:format(e.min,'###,##0.#')}</td>
							<td>${w:format(e.max,'###,##0.#')}</td>
							<td>${w:format(e.avg,'###,##0.0')}</td>
							<td>${w:format(e.line95Value,'###,##0.0')}</td>
							<td>${w:format(e.line99Value,'###,##0.0')}</td>
							<td>${w:format(e.std,'###,##0.0')}</td>
							<td>${w:format(e.tps,'###,##0.0')}</td>
						</tr>
					</c:forEach>
					</tbody>
				</table>
			</div>
		</c:forEach>
	</div>
</div>

<script>
	$(document).ready(function(){
		var fistNavTav = $(".navTabs").first();
		var id = fistNavTav.text();
		
		fistNavTav.addClass("active");
		$("#"+id).addClass("active");
	})
</script>
