<%@ page contentType="text/html; charset=utf-8" %>
 <div class="row-fluid">
 		<div class="span6">
 			<h5 class="text-error text-center">项目本身详细数据</h5>
 			<table	class="contents table table-striped table-bordered table-condensed">
			<thead>	<tr>
				<th>Name</th>
				<th>Total Count</th>
				<th>Failure Count</th>
				<th>Failure%</th>
				<th>Avg(ms)</th>
				<th>Config</th>
			</tr></thead><tbody>
			<c:forEach var="item" items="${model.segment.indexs}" varStatus="status">
				 <c:set var="itemKey" value="${item.key}" />
				 <c:set var="itemValue" value="${item.value}" />
				<tr>
					<td>${itemValue.name}</td>
					<td style="text-align:right;">${itemValue.totalCount}</td>
					<td style="text-align:right;">${itemValue.errorCount}</td>
					<td style="text-align:right;">${w:format(itemValue.errorCount/itemValue.totalCount,'0.0000')}</td>
					<td style="text-align:right;">${w:format(itemValue.avg,'0.0')}</td>
					<td><a class="btn btn-primary btn-small" target="_blank" href="/cat/s/config?op=topologyGraphNodeConfigAdd&type=${itemValue.name}&domain=${model.domain}">配置阀值</a></td>
				</tr>		
			</c:forEach></tbody>
		</table>
 		</div>
 		<div class="span6">
 			<h5 class="text-error text-center">依赖项目详细数据</h5>
		<table class="contentsDependency table table-striped table-bordered table-condensed">
			<thead>	<tr>
				<th>Type</th>
				<th>Target</th>
				<th>Total Count</th>
				<th>Failure Count</th>
				<th>Failure%</th>
				<th>Avg(ms)</th>
				<th>Config</th>
			</tr></thead><tbody>
			<c:forEach var="item" items="${model.segment.dependencies}" varStatus="status">
				 <c:set var="itemKey" value="${item.key}" />
				 <c:set var="itemValue" value="${item.value}" />
				<tr>
					<td>${itemValue.type}</td>
					<td>${itemValue.target}</td>
					<td style="text-align:right;">${itemValue.totalCount}</td>
					<td style="text-align:right;">${itemValue.errorCount}</td>
					<td style="text-align:right;">${w:format(itemValue.errorCount/itemValue.totalCount,'0.0000')}</td>
					<td style="text-align:right;">${w:format(itemValue.avg,'0.0')}</td>
					<td><a class="btn btn-primary btn-small" target="_blank" href="/cat/s/config?op=topologyGraphEdgeConfigAdd&type=${itemValue.type}&from=${model.domain}&to=${itemValue.target}">配置阀值</a></td>
				</tr>		
			</c:forEach></tbody>
		</table>	  			
 		</div>
 </div>