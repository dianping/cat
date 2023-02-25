<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:choose>
	<c:when test="${empty model.table}">
		<div class="error"></div>抱歉，消息可能是丢失或者已存档。</div>
	</c:when>
	<c:otherwise>${model.table}</c:otherwise>
</c:choose>