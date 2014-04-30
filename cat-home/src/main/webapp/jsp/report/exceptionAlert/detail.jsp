	<%@ page session="false" language="java" pageEncoding="UTF-8" %>
	<br/>
	<table class="table table-striped table-bordered table-condensed table-hover">
	<tr>
			<th width="10%">项目组</th>
			<th width="70%">异常名称</th>
			<th width="10%">Warn告警</th>
			<th width=10%>Error告警</th>
	</tr>
	<c:forEach var="domain" items="${model.exceptions}">	
 		<c:forEach var="exception" items="${domain.value}" varStatus="status">
			<c:if test="${status.index ==0 }">
				<tr>
					<td  rowspan="${w:size(domain.value)}"><span>${domain.key}</span></td>
					<td>${exception.id}</td>
					<td>${exception.warnNumber}</td>
					<td>${exception.errorNumber}</td>
				</c:if>
				</tr>
			<c:if test="${status.index >0 }">
				<tr>
					<td>${exception.id}</td>
					<td>${exception.warnNumber}</td>
					<td>${exception.errorNumber}</td>
				</tr>
			</c:if>
		</c:forEach>
	</c:forEach>
	</table>