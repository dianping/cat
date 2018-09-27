<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.web.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.web.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.web.Model" scope="request"/>

<a:web_body>
		<h3 class="text-center text-success">编辑JS告警规则</h3>
		<form name="jsRuleUpdate" id="form" method="post" action="${model.pageUri}?op=jsRuleUpdateSubmit">
			<table style='width:100%' class='table table-striped table-condensed '>
				<input type="hidden" class="input-xlarge"  name="jsRule.id" value="${model.jsRule.id}" />
				<tr>
					<td>模块名称</td>
					<td>
					<c:choose>
					<c:when test="${fn:length(model.jsRule.id) eq 0}">
						<span class="input-icon" style="width:270px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" name="jsRule.domain" id="module" autocomplete="on" data="" required/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</c:when>
					<c:otherwise>
			  		<input type="text" class="input-xlarge" placeholder="前端模块名称" name="jsRule.domain"  value="${model.jsRule.domain}" readonly/>
					</c:otherwise>
					</c:choose>
					</td>
				</tr>
				<tr>
					<td>报错等级</td>
					<td>
					<c:choose>
					<c:when test="${fn:length(model.jsRule.id) eq 0}">
						<select id="level" style="width:270px" name="jsRule.level">
						<c:forEach var="level" items="${model.levels}">
							<option value="${level}">${level}</option>
						</c:forEach>
						</select>				
					</c:when>
					<c:otherwise>
		            	<input type="text" class="input-xlarge"  name="jsRule.level" value="${model.jsRule.level}" readonly/>
					</c:otherwise>
					</c:choose>
					</td>
				</tr>
				<tr>
					<td>告警阈值</td>
					<td><input type="text" class="input-xlarge"  placeholder="告警阈值" name="jsRule.limit" required value="${model.jsRule.limit}"/> / 分钟</td>
				</tr>
				<tr>
					<td>联系邮件</td>
					<td><input type="text" class="input-xlarge"  placeholder="联系邮件" name="jsRule.mails" required value="${model.jsRule.mails}"/>（多个以逗号隔开）</td>
				</tr>
				
				<tr>
					<td style='text-align:center' colspan='2'><input class='btn btn-primary btn-sm' type="submit" name="submit" value="提交" /></td>
				</tr>
			</table>
		</form>
		
<script type="text/javascript">
	$(document).ready(function() {
		$('#Web_config').addClass('active open');
		$('#jsRule').addClass('active');
		 
		//custom autocomplete (category selection)
		$.widget( "custom.catcomplete", $.ui.autocomplete, {
			_renderMenu: function( ul, items ) {
				var that = this,
				currentCategory = "";
				$.each( items, function( index, item ) {
					if ( item.category != currentCategory ) {
						ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
						currentCategory = item.category;
					}
					that._renderItemData( ul, item );
				});
			}
		});
		
		var data = [];
		<c:forEach var="module" items="${model.modules}">
			var item = {};
			item['label'] = '${module}';
			item['category'] ="modules";
			data.push(item);
		</c:forEach>
		
		$("#module").catcomplete({
			delay: 0,
			source: data
		});
		
	});
</script> 
</a:web_body>