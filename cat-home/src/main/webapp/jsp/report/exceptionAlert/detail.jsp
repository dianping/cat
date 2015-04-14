<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<table class="table table-striped table-condensed   table-hover" id="contents" width="100%">
	<thead>
		<tr >
			<th width="40%">域名</th>
			<th width="20%">Warning警告</th>
			<th width="20%">Error警告</th>
			<th width="20%">Detail</th>
		</tr>
	</thead>
	
	<tbody>
		<c:forEach var="domain" items="${model.alertDomains}">	
			<tr>
				<td>${domain.name}</td>
				<td>${domain.warnNumber}</td>
				<td>${domain.errorNumber}</td>
				<td><a class='detail btn btn-primary btn-xs' href="?op=alertDetail&domain=${domain.name}&date=${model.date}">Detail</a></td>
				</tr>
			</c:forEach>
	</tbody>
</table>
