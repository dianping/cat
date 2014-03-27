<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html; charset=utf-8"%>
	<div class="text-left"></div>
	开始
	<input type="text" name="startTime" id="startTime" value="<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>" style="height:auto" class="input-medium" placeholder="格式如：2014-02-02 00:00:00">
	结束
    <input type="text" name="endTime" id="endTime" value="<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>" style="height:auto" class="input-medium" placeholder="格式如：2014-02-02 00:00:00">
	应用名
	<input type="text" name="domain" id="domain" value="${payload.domain}" style="height:auto" class="input-small">
	机器名
	<input type="text" name="hostname" id="hostname" value="${payload.hostname}" style="height:auto" class="input-small"> 
	<input class="btn btn-primary  btn-small"  value="查询" onclick="queryNew()" type="submit">
		
	<c:if test="${!payload.fullScreen}">
			<a id="fullScreen" class='btn btn-small btn-primary' onclick="queryFullScreen(true)">全屏</a>
	</c:if>
	<c:if test="${payload.fullScreen}">
			<a id="fullScreen" class='btn btn-small btn-primary' onclick="queryFullScreen(false)">全屏</a>
	</c:if>
	<a id="refresh10" class='btn btn-small btn-primary' onclick="queryFrequency(10)">10秒</a>
	<a id="refresh20" class='btn btn-small btn-primary' onclick="queryFrequency(20)">20秒</a>
	<a id="refresh30" class='btn btn-small btn-primary' onclick="queryFrequency(30)">30秒</a>
	<br>
	<div class="btn-group">
	  
	</div>
	</div>

<script>
	function queryNew(){
		var startTime=$("#startTime").val();
		var endTime=$("#endTime").val();
		var domain=$("#domain").val();
		var hostname=$("#hostname").val();
		window.location.href="?op=view&domain="+domain+"&startTime="+startTime+"&endTime="+endTime+"&hostname="+hostname;
	}
	function queryFullScreen(isFullScreen){
		<c:if test="${payload.refresh}">
			window.location.href="?domain=${payload.domain}&hostname=${payload.hostname}&fullScreen="+isFullScreen+"&refresh=${payload.refresh}&frequency=${payload.frequency}";
		</c:if>
		<c:if test="${!payload.refresh}">
			window.location.href="?domain=${payload.domain}&hostname=${payload.hostname}&fullScreen="+isFullScreen+"&refresh=${payload.refresh}&frequency=${payload.frequency}&startTime=<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&endTime=<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>";
		</c:if>
	}
	function queryFrequency(frequency){
		window.location.href="?domain=${payload.domain}&hostname=${payload.hostname}&fullScreen=${payload.fullScreen}&refresh=true&frequency="+frequency;
	}
</script>