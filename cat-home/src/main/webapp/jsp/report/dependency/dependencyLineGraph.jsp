<%@ page contentType="text/html; charset=utf-8" %>
<div id="fullScreenData">
<div class="row-fluid">
	<div class='span12'>
		<h4 class='text-danger text-left' style="margin-left: 10px;"><b>项目指标趋势图</b></h4>
		<table>
			<tr>
				<c:forEach var="item" items="${model.indexGraph}" varStatus="status">
					<td>
						<div id="item${status.index}" style="width:380px;height:300px;"></div>
					</td>
				</c:forEach>
			</tr>
		</table>
	</div>
</div>
<div class="row-fluid">
	<div class='span12'>
		<h4 class='text-danger text-left' style="margin-left: 10px;"><b>项目依赖组件指标趋势图</b></h4>
		<table>
		<c:forEach var="charts" items="${model.dependencyGraph}" varStatus="type">
			<th colspan="3"><b><h5 class='text-left text-info' style="margin-left: 10px;">${charts.key}</h5></b></th>
			<c:set var="key" value="${charts.key}"/>
			<c:set var="value" value="${charts.value}"/>
			<tr>
			<c:forEach var="item" items="${value}" varStatus="status">
					<td>
						<div id="item${type.index}-${status.index}" style="width:380px;height:300px;"></div></td>
			</c:forEach></tr>
		</c:forEach></table>
	</div>
</div>
</div>
<script type="text/javascript">
	<c:forEach  var="item" items="${model.indexGraph}" varStatus="status">
		graphLineChart(document.getElementById('item${status.index}'),${item});
	</c:forEach>
	<c:forEach var="charts" items="${model.dependencyGraph}" varStatus="type">
		<c:set var="key" value="${charts.key}"/>
		<c:set var="value" value="${charts.value}"/>
		<c:forEach var="item" items="${value}" varStatus="status">
			graphLineChart(document.getElementById('item${type.index}-${status.index}'),${item});
		</c:forEach>
</c:forEach>
$(document).ready(function() {
	$('#dependency_trend').addClass('active');
});
</script>


