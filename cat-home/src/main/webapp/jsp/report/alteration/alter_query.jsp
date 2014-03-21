<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html; charset=utf-8"%>
	<div class="text-left text-info"></div>时间粒度 <select class="input-small"  id="granularity">
			<option value="1000">1 sec</option>
			<option value="5000">5 sec</option>
			<option value="60000">1 min</option>
			<option value="300000">5 min</option>
			<option value="3600000">1 hour</option>
			<option value="86400000">1 day</option>			
	</select>
	开始
	<input type="text" name="startTime" id="startTime" value="<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>" style="height:auto" class="input-medium" placeholder="格式如：2014-02-02 00:00:00">
	结束
       <input type="text" name="endTime" id="endTime" value="<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>" style="height:auto" class="input-medium" placeholder="格式如：2014-02-02 00:00:00">
	应用名
	<input type="text" name="domain" id="domain" value="${payload.domain}" style="height:auto" class="input-small">
	机器名
	<input type="text" name="hostname" id="hostname" value="${payload.hostname}" style="height:auto" class="input-small"> 
	
	<input class="btn btn-primary  btn-small"  value="查询"
		onclick="queryNew()" type="submit">
		
	<c:if test="${!payload.fullScreen}">
			<a id="fullScreen" class='btn btn-small btn-primary' href="?fullScreen=true&refresh=${payload.refresh}&frequency=${payload.frequency}&startTime=<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&endTime=<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&granularity=${payload.granularity}">全屏</a>
	</c:if>
	<c:if test="${payload.fullScreen}">
			<a id="fullScreen" class='btn btn-small btn-primary' href="?fullScreen=false&refresh=${payload.refresh}&frequency=${payload.frequency}&startTime=<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&endTime=<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&granularity=${payload.granularity}">全屏</a>
	</c:if>
	<a id="refresh10" class='btn btn-small btn-primary' href="?fullScreen=${payload.fullScreen}&refresh=true&frequency=10&startTime=<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&endTime=<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&granularity=${payload.granularity}">10秒定时刷新</a>
	<a id="refresh20" class='btn btn-small btn-primary' href="?fullScreen=${payload.fullScreen}&refresh=true&frequency=20&startTime=<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&endTime=<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&granularity=${payload.granularity}">20秒定时刷新</a>
	<a id="refresh30" class='btn btn-small btn-primary' href="?fullScreen=${payload.fullScreen}&refresh=true&frequency=30&startTime=<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&endTime=<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&granularity=${payload.granularity}">30秒定时刷新</a>
</div>

<script>
	function queryNew(){
		var granularity=$("#granularity").val();
		var startTime=$("#startTime").val();
		var endTime=$("#endTime").val();
		var domain=$("#domain").val();
		var hostname=$("#hostname").val();
		window.location.href="?op=view&domain="+domain+"&granularity="+granularity+"&startTime="+startTime+"&endTime="+endTime+"&hostname="+hostname;
	}
</script>

