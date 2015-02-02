<%@ page contentType="text/html; charset=utf-8" %>
<div class="text-center">
<ul class="pagination">
<c:forEach var="item" items="${model.minutes}" varStatus="status">
		
		<c:if test="${item > model.maxMinute }"><li class="disabled" id="minute${item}"><a
		class="href${item}" href='javascript:void(0)' disabled='true'>
			<c:if test="${item < 10}">0${item}</c:if>
			<c:if test="${item >= 10}">${item}</c:if></a></li>
		</c:if>
		<c:if test="${item <= model.maxMinute }"><li id="minute${item}"><a
		class="href${item}" href="?op=${payload.action.name}&domain=${model.domain}&date=${model.date}&minute=${item}&fullScreen=${payload.fullScreen}&refresh=${payload.refresh}&frequency=${payload.frequency}">
			<c:if test="${item < 10}">0${item}</c:if>
			<c:if test="${item >= 10}">${item}</c:if></a></li>
		</c:if>
</c:forEach>
</ul>
</div>
<style>
.pagination {
	margin: 4px 0;
}
.pagination ul > li > a {
	padding: 4px 10px;
}
</style>

<script type="text/javascript">
	$('.href${model.minute}').css('color',"red");
	$('.href${model.minute}').css('font-weight',"bold");
</script>