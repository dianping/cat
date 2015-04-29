<%@ page contentType="text/html; charset=utf-8" %>
<style>
.pagination{
		margin:4px 0;
	}
	.pagination ul{
		margin-top:0px;
	}
	.pagination ul > li > a, .pagination ul > li > span{
		padding:3px 10px;
	}
</style>
<ul class="pagination">
<c:forEach var="item" items="${model.minutes}" varStatus="status">
		<c:if test="${item > model.maxMinute }"><li class="disabled" id="minute${item}"><a
		class="href${item}" href="?op=${payload.action.name}&domain=${model.domain}&date=${model.date}&minute=${item}&fullScreen=${payload.fullScreen}&refresh=${payload.refresh}&frequency=${payload.frequency}&type=${payload.type}">
			<c:if test="${item < 10}">0${item}</c:if>
			<c:if test="${item >= 10}">${item}</c:if></a></li>
		</c:if>
		<c:if test="${item <= model.maxMinute }"><li id="minute${item}"><a
		class="href${item}" href="?op=${payload.action.name}&domain=${model.domain}&date=${model.date}&minute=${item}&fullScreen=${payload.fullScreen}&refresh=${payload.refresh}&frequency=${payload.frequency}&type=${payload.type}">
			<c:if test="${item < 10}">0${item}</c:if>
			<c:if test="${item >= 10}">${item}</c:if></a></li>
		</c:if>
</c:forEach>
</ul>
<script type="text/javascript">
	$('.href${model.minute}').css('color',"red");
	$('.href${model.minute}').css('font-weight',"bold");
</script>