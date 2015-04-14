<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="span10">
    <style type="text/css">
  .modal-body {
    position: relative;
    overflow-y: auto;
    max-height: 400px;
    padding: 15px;
}
    </style>

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
					<td><a class='detail btn btn-primary btn-sm' href="?op=historyAlertDetail&domain=${domain.name}&startDate=${payload.historyStartDate}&endDate=${payload.historyEndDate}">Detail</a></td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
