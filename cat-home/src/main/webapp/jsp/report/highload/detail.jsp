<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<br/>
<style>
	.tableHeader:hover {
		cursor:hand;
	}
</style>
<div class="tabbable "  > <!-- Only required for left/right tabs -->
	<ul class="nav nav-tabs">
		<c:forEach var="type" items="${model.report.types}" varStatus="status">
			<li class="text-right navTabs" id="${type.id}"><a href="#${type.id}Content" data-toggle="tab"><strong>${type.id}</strong></a></li>
		</c:forEach>
	</ul>
	<div class="tab-content">
		<c:forEach var="type" items="${model.report.types}" varStatus="status">
			<div class="tab-pane" id="${type.id}Content">
				<table	class="problem table table-striped table-bordered table-condensed table-hover">
					<thead>
					<tr class="text-success">
						<th width="8%" class="tableHeader" data-sortBy="domain">项目</th>
						<th width="20%" class="tableHeader" data-sortBy="name">名称</th>
						<th width="5%" class="tableHeader" data-sortBy="total">总数</th>
						<th width="5%" class="tableHeader" data-sortBy="error">错误数</th>
						<th width="7%" class="tableHeader" data-sortBy="failure">失败率</th>
						<th width="4%" class="tableHeader" data-sortBy="min">Min</th>
						<th width="5%" class="tableHeader" data-sortBy="max">Max</th>
						<th width="5%" class="tableHeader" data-sortBy="avg">Avg</th>
						<th width="5%" class="tableHeader" data-sortBy="95line">95Line</th>
						<th width="6%" class="tableHeader" data-sortBy="999line">99.9Line</th>
						<th width="5%" class="tableHeader" data-sortBy="std">Std</th>
					</tr>
					</thead>
					<tbody>
					<c:forEach var="e" items="${type.names}" varStatus="status">
						<tr>
							<td>${e.domain}</td>
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
		var payloadType = "${payload.type}";
		if(payloadType==""){
			var fistNavTav = $(".navTabs").first();
			var id = fistNavTav.prop("id");
			
			fistNavTav.addClass("active");
			$("#"+id+"Content").addClass("active");
		}else{
			$("#"+payloadType).addClass('active');
			$("#"+payloadType+"Content").addClass('active');
		}
		
		$(".tableHeader").click(function(){
			var date = $("#time").val();
			var type = $(".navTabs").filter(".active").prop("id");
			var sortBy = $(this).attr("data-sortBy");
			
			window.location.href="?date="+date+"&type="+type+"&sortBy="+sortBy;
		});
	})
</script>
