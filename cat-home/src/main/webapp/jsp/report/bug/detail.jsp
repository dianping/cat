<%@ page session="false" language="java" pageEncoding="UTF-8" %>
	<table class="table table-striped table-condensed   table-hover">
	<tr>
			<th width="10%">部门</th>
			<th width="10%">产品线</th>
			<th width="70%">异常名称</th>
			<th width="10%">异常数量</th>
		</tr>
	<c:forEach var="item" items="${model.errorStatis}">
		<c:set var="statis" value="${item.value}"/>
		<c:forEach var="bug" items="${statis.bugs}" varStatus="status">
			<c:if test="${status.index ==0 }">
				<tr>
					<td  rowspan="${w:size(statis.bugs)}"><span>${statis.department}</span></td>
					<td  rowspan="${w:size(statis.bugs)}"><span>${statis.productLine}</span></td>
					<td>${bug.value.id}</td>
					<td style="text-align:right">${bug.value.count}</td>
				</c:if>
			</tr>
			<c:if test="${status.index >0 }">
				<tr>
					<td>${bug.value.id}</td>
					<td style="text-align:right">${bug.value.count}</td>
				</tr>
			</c:if>
		</c:forEach>
	</c:forEach>
	</table>
	<%-- <h4 class='text-info'>项目其他异常数据(比如框架类异常，超时等)</h4>
	<table class="table table-striped table-condensed   table-hover">
	<tr>
			<th width="10%">部门</th>
			<th width="10%">产品线</th>
			<th width="60%">异常名称</th>
			<th width="10%">异常数量</th>
		</tr>
		<c:forEach var="item" items="${model.errorStatis}">
		<c:set var="statis" value="${item.value}"/>
		<c:forEach var="exception" items="${statis.exceptions}" varStatus="status">
			<c:if test="${status.index ==0 }">
				<tr>
					<td  rowspan="${w:size(statis.exceptions)}"><span>${statis.department}</span></td>
					<td  rowspan="${w:size(statis.exceptions)}"><span>${statis.productLine}</span></td>
					<td>${exception.value.id}</td>
					<td style="text-align:right">${exception.value.count}</td>
				</c:if>
			</tr>
			<c:if test="${status.index >0 }">
				<tr>
					<td>${exception.value.id}</td>
					<td style="text-align:right">${exception.value.count}</td>
				</tr>
			</c:if>
		</c:forEach></c:forEach>
	</table> --%>