<%@ page contentType="text/html; charset=utf-8" %>
<div class="tabbable"  id="otherDependency">
	  <ul class="nav nav-tabs">
	  	<c:forEach  var="item" items="${model.events}"  varStatus="status" >
			 <li id="leftTab${status.index}" class="text-right"><a href="#tab${status.index}" data-toggle="tab">
			 ${item.key}
			 <c:set var="size" value="${w:size(item.value)}"/>
			 <c:if test="${size > 0 }"><span class='text-error'>(${size})</span></c:if>
		</a></li>
	  	</c:forEach>
	  </ul>
  	<div class="tab-content">
   		<c:forEach  var="entry" items="${model.events}"  varStatus="status" >
   		<c:set var="items" value="${entry.value}"/>
		    <div class="tab-pane" id="tab${status.index}">	
				<table	class="table table-striped table-bordered table-condensed">
		  		<thead>
		  			<tr><th>时间</th>
		  				<th>标题</th>
		  				<th>详情</th>
		  				<th>来源</th>
		  				<th>项目名</th>
		  				<th>IP</th>
		  			</tr>
		  		</thead>	
		  		<tbody>
		  			<c:forEach var="item" items="${items}">
		  				<tr><td>${w:format(item.date,'HH:mm')}</td>
		  					<td>
		  						<c:choose>
		  							<c:when test="${not empty item.link}"><a href="${item.link}" target="_blank">${item.subject}</a></c:when>
		  							<c:otherwise>${item.subject}</c:otherwise>
		  						</c:choose>
		  					</td>
		  					<td>${item.content}</td>
		  					<td>
		  					<c:choose>
		  						<c:when test="${item.type==1}">运维</c:when>
		  						<c:when test="${item.type==2}">数据库</c:when>
		  						<c:when test="${item.type==3}">CAT</c:when>
		  					</c:choose>
		  					</td>
		  					<td>${item.domain}</td>
		  					<td>${item.ip}</td>
		  				</tr>
		  			</c:forEach>	
			</table></div>
			</c:forEach>
	    </div>
    </div>