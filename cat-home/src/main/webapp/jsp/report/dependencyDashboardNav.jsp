<%@ page contentType="text/html; charset=utf-8" %>

<div class='text-center'>
  <a class="btn btn-danger  btn-primary" href="?minute=${model.minute}&domain=${model.domain}&date=${model.date}">切换到实时趋势图（当前分钟:${model.minute}）</a>
 	  <c:forEach var="item" items="${model.minutes}" varStatus="status">
	<c:if test="${status.index % 30 ==0}">
		<div class="pagination">
		<ul>
	</c:if>
		<c:if test="${item > model.maxMinute }"><li class="disabled" id="minute${item}"><a
		href="?op=dependencyGraph&domain=${model.domain}&date=${model.date}&minute=${item}">
			<c:if test="${item < 10}">0${item}</c:if>
			<c:if test="${item >= 10}">${item}</c:if></a></li>
		</c:if>
		<c:if test="${item <= model.maxMinute }"><li id="minute${item}"><a
		href="?op=dependencyGraph&domain=${model.domain}&date=${model.date}&minute=${item}">
			<c:if test="${item < 10}">0${item}</c:if>
			<c:if test="${item >= 10}">${item}</c:if></a></li>
		</c:if>
	<c:if test="${status.index % 30 ==29 || status.last}">
		</ul>
		</div>
	</c:if>
	</c:forEach>
</div>