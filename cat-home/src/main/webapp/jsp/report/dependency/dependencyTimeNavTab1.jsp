<%@ page contentType="text/html; charset=utf-8" %>
<c:forEach var="item" items="${model.minutes}" varStatus="status">
	<c:if test="${status.index % 30 ==0}">
		<div class="pagination">
		<ul>
	</c:if>
		<c:if test="${item > model.maxMinute }"><li class="disabled" id="minute${item}"><a
		class="href${item}" href="?op=${payload.action.name}&domain=${model.domain}&date=${model.date}&minute=${item}&productLine=${payload.productLine}&fullScreen=${payload.fullScreen}&refresh=${payload.refresh}&frequency=${payload.frequency}">
			<c:if test="${item < 10}">0${item}</c:if>
			<c:if test="${item >= 10}">${item}</c:if></a></li>
		</c:if>
		<c:if test="${item <= model.maxMinute }"><li id="minute${item}"><a
		class="href${item}" href="?op=${payload.action.name}&domain=${model.domain}&date=${model.date}&minute=${item}&productLine=${payload.productLine}&fullScreen=${payload.fullScreen}&refresh=${payload.refresh}&frequency=${payload.frequency}">
			<c:if test="${item < 10}">0${item}</c:if>
			<c:if test="${item >= 10}">${item}</c:if></a></li>
		</c:if>
	<c:if test="${status.index % 30 ==29 || status.last}">
		</ul>
		</div>
	</c:if>
</c:forEach>

<script type="text/javascript">
	$('.href${model.minute}').css('color',"red");
	$('.href${model.minute}').css('font-weight',"bold");
</script>