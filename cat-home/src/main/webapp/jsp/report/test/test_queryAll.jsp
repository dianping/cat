<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="model" type="com.dianping.cat.report.page.test.Model" scope="request"/>
<c:forEach var="item" items="${model.families}" varStatus="status">
	My name is ${item.name}, I'm ${item.age} years old<br>
</c:forEach>