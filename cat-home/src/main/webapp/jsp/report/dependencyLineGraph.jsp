<%@ page contentType="text/html; charset=utf-8" %>
	
<div class="row-fluid">
	<div class='span12'>
		<h3 class='text-error'>项目本身指标趋势图</h3>
		<table>
			<tr>
				<c:forEach var="item" items="${model.indexGraph}" varStatus="status">
					<td><div id="item${status.index}" style="width:450px;height:200px;" class="graph"></div></td>
				</c:forEach>
			</tr>
		</table>
	</div>
</div>
<div class="row-fluid">
	<div class='span12'>
		<h3 class='text-error'>项目依赖指标趋势图</h3>
		<table>
		<c:forEach var="charts" items="${model.dependencyGraph}" varStatus="type">
			<th colspan="3"><h4 class='text-center text-warning'>${charts.key}</h4></th>
			<c:set var="key" value="${charts.key}"/>
			<c:set var="value" value="${charts.value}"/>
			<tr>
			<c:forEach var="item" items="${value}" varStatus="status">
					<td><div id="item${type.index}-${status.index}" style="width:450px;height:200px;" class="graph"></div></td>
			</c:forEach></tr>
		</c:forEach></table>
	</div>
</div>

<script type="text/javascript">
	<c:forEach  var="item" items="${model.indexGraph}" varStatus="status">
		graph(document.getElementById('item${status.index}'), ${item});	
	</c:forEach>
	<c:forEach var="charts" items="${model.dependencyGraph}" varStatus="type">
		<c:set var="key" value="${charts.key}"/>
		<c:set var="value" value="${charts.value}"/>
		<c:forEach var="item" items="${value}" varStatus="status">
			graph(document.getElementById('item${type.index}-${status.index}'), ${item});	
		</c:forEach>
</c:forEach>
</script>


