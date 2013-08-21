	<%@ page session="false" language="java" pageEncoding="UTF-8" %>
	 <div class="text-right">
		 <a class="btn  btn-small btn-danger" target='_blank' href="/cat/s/config?op=bugConfigUpdate">异常规范配置</a>
	 </div>
	<h3 class='text-error'>项目Bug异常数据</h3>
	<table class="table table-striped table-bordered table-condensed">
	<tr>
			<th width="10%">部门</th>
			<th width="10%">产品线</th>
			<th width="60%">异常名称</th>
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
					<td>${bug.value.count}</td>
				</c:if>
			</tr>
			<c:if test="${status.index >0 }">
				<tr>
					<td>${bug.value.id}</td>
					<td>${bug.value.count}</td>
				</tr>
			</c:if>
		</c:forEach>
	</c:forEach>
	</table>
	
	<h3 class='text-info'>项目其他异常数据(比如框架类异常，超时等)</h3>
	<table class="table table-striped table-bordered table-condensed">
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
					<td>${exception.value.count}</td>
				</c:if>
			</tr>
			<c:if test="${status.index >0 }">
				<tr>
					<td>${exception.value.id}</td>
					<td>${exception.value.count}</td>
				</tr>
			</c:if>
		</c:forEach></c:forEach>
	</table>