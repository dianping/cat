<%@ page contentType="text/html; charset=utf-8"%>
<table class="problem">
<tr>
		<th>Type</th>
		<th>Total</th>
		<th>Status</th>
		<th>Count</th>
		<th>SampleLinks</th>
	</tr>
	<c:forEach var="statistics" items="${model.allStatistics.status}" varStatus="typeIndex">
		<tr>
			<td rowspan="${w:size(statistics.value.status)}" class="${typeIndex.index mod 2 != 0 ? 'even' : 'odd'}"><a href="#"
						class="${statistics.value.type}">&nbsp;&nbsp;</a>
				&nbsp;&nbsp;${statistics.value.type}
			</td>
			<td rowspan="${w:size(statistics.value.status)}" class="${typeIndex.index mod 2 != 0 ? 'even' : 'odd'}">${statistics.value.count}</td>
			<c:forEach var="status" items="${statistics.value.status}"
						varStatus="index">
				<c:if test="${index.index != 0}">
					<tr>
				</c:if>
				<td class="${index.index mod 2 != 0 ? 'even' : 'odd'}">${status.value.status}</td>
				<td class="${index.index mod 2 != 0 ? 'even' : 'odd'}">${status.value.count}</td>
				<td class="${index.index mod 2 != 0 ? 'even' : 'odd'}"><c:forEach
								var="links" items="${status.value.links}" varStatus="linkIndex">
						<a href="${model.logViewBaseUri}/${links}">
							<c:if test="${linkIndex.first}">
								L
							</c:if>
							<c:if test="${linkIndex.first==false&&linkIndex.last}">
								G
							</c:if>
							<c:if test="${linkIndex.first==false&&linkIndex.last==false}">
								O
							</c:if>
						</a>
					</c:forEach>
				</td>
				<c:if test="${index.index != 0}">
				</tr>
				</c:if>
			</c:forEach>
			</tr>
	</c:forEach>
</table>