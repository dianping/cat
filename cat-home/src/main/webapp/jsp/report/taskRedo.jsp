<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="model" type="com.dianping.cat.report.page.task.Model" scope="request" />
${model.redoResult} 

<c:if test="${model.redoResult==true}">redo successful!</c:if>
<c:if test="${model.redoResult==false}">redo failed!</c:if>







