<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<script src="${model.webapp}/assets/js/bootstrap-tag.min.js"></script>
	<h3 class="text-center text-success">编辑第三方监控规则</h3>
	<form name="thirdPartyRuleSubmit" id="form" method="get" action="${model.pageUri}">
	<table class="table table-striped table-condensed ">
		<input type="hidden" name="type" value="${payload.type}" />
		<input type="hidden" name="op" value="thirdPartyRuleSubmit" />
		<c:if test="${payload.type eq 'http'}">
		<tr>
			<td width="10%">url</td>
			<c:choose>
			<c:when test="${not empty model.http.url }">
				<td><input type="name" name="http.url" size="50" value="${model.http.url}" readonly/></td>
			</c:when>
			<c:otherwise>
				<td><input type="name" name="http.url" size="50" value="${model.http.url}"/></td>
			</c:otherwise>
			</c:choose>
			<td>监控的对象，字符串类型</td>
		</tr>
		<tr>
			<td width="10%">类型</td>
			<td>
			<select name="http.type" id="typeSelect" style="width:200px;">
                	<option value="get">get</option>
                	<option value="post">post</option> 							
            	</select>
            </td>
			<td>URL访问类型</td>
		</tr>
		<tr>
			<td width="10%">项目组</td>
			<td><input type="name" name="http.domain" value="${model.http.domain}"/></td>
			<td>依赖于该第三方的项目名，会向该项目组联系人发第三方告警，字符串类型</td>
		</tr>
		<tr>
		<td width="10%">参数</td>
		<td>
			<input type="text" name="pars" id="form-field-tags" placeholder="Enter pars eg. key=value..."/>
		</td>
		<td>请求中包含的参数，如：date=2014073111，多个请输入后回车添加</td>
		</c:if>

		<c:if test="${payload.type eq 'socket'}">
		<tr>
			<td width="10%">Ip</td>
			<c:choose>
			<c:when test="${not empty model.socket.ip }">
				<td><input type="name" name="socket.ip" value="${model.socket.ip}" readonly/></td>
			</c:when>
			<c:otherwise>
				<td><input type="name" name="socket.ip" value="${model.socket.ip}"/></td>
			</c:otherwise>
			</c:choose>
			<td>Ip地址，字符串格式</td>
		</tr>
		<tr>
			<td width="10%">端口</td>
			<c:choose>
			<c:when test="${not empty model.socket.port }">
				<td><input type="name" name="socket.port" value="${model.socket.port}" readonly/></td>
			</c:when>
			<c:otherwise>
				<td><input type="name" name="socket.port" value="${model.socket.port}"/></td>
			</c:otherwise>
			</c:choose>
			<td>端口号，整型数字</td>
		</tr>
		<tr>
			<td width="10%">项目组</td>
			<td><input type="name" name= "socket.domain" value="${model.socket.domain}"/></td>
			<td>依赖于该第三方的项目名，会向该项目组联系人发第三方告警，字符串类型</td>
		</tr>
		</c:if>
		<tr>
			<td colspan="3" align="center"><input class='btn btn-primary btn-sm' type="submit" name="submit" value="提交" /></td>
		</tr>
	</table>
</form>
</a:config>

<script type="text/javascript">
$(document).ready(function() {
	$('#alert_config').addClass('active open');
	$('#thirdPartyConfigUpdate').addClass('active');
	<c:if test="${not empty model.http.type}">
	$('#typeSelect').val("${model.http.type}");
	</c:if>
	var tag_input = $('#form-field-tags');
	try{
		tag_input.tag(
		  {
			placeholder:tag_input.attr('placeholder'),
			//enable typeahead by specifying the source array
			//source: ace.vars['US_STATES'],//defined in ace.js >> ace.enable_search_ahead
			/**
			//or fetch data from database, fetch those that match "query"
			source: function(query, process) {
			  $.ajax({url: 'remote_source.php?q='+encodeURIComponent(query)})
			  .done(function(result_items){
				process(result_items);
			  });
			}
			*/
		  }
		)

		//programmatically add a new
		var $tag_obj = $('#form-field-tags').data('tag');
		<c:if test="${payload.type eq 'http'}">
		<c:forEach var="item" items="${model.http.pars}" varStatus="status">
			$tag_obj.add("${item.id}");
		</c:forEach>
		</c:if>
	}
	catch(e) {
		//display a textarea for old IE, because it doesn't support this plugin or another one I tried!
		tag_input.after('<textarea id="'+tag_input.attr('id')+'" name="'+tag_input.attr('name')+'" rows="3">'+tag_input.val()+'</textarea>').remove();
		//$('#form-field-tags').autosize({append: "\n"});
	}
	
});
</script>
<style>
.tags {
	width:auto;
}
</style>