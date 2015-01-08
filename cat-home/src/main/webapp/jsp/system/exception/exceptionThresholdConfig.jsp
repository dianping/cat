<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>
<a:config>
<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
<res:useCss value="${res.css.local['jqx.base.css']}" target="head-css" />
<res:useJs value="${res.js.local['jqxcore.js']}" target="head-js" />
<res:useJs value="${res.js.local['jqxbuttons.js']}" target="head-js" />
<res:useJs value="${res.js.local['jqxscrollbar.js']}" target="head-js" />
<res:useJs value="${res.js.local['jqxlistbox.js']}" target="head-js" />
<res:useJs value="${res.js.local['jqxcombobox.js']}" target="head-js" />
<script type="text/javascript">
		$(document).ready(function() {
		$('#application_config').addClass('active open');
		$('#exception').addClass('active');

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
		<c:forEach var="item" items="${model.exceptionList}">
					var item = {};
					item['label'] = '${item}';
					item['category'] = '异常名';
					data.push(item);
		</c:forEach>
				
		$( "#search_exception" ).catcomplete({
			delay: 0,
			source: data
		});
		
		data = [];
		<c:forEach var="item" items="${model.domainList}">
					var item = {};
					item['label'] = '${item}';
					item['category'] = '项目名';
					data.push(item);
		</c:forEach>
				
		$( "#search_domain" ).catcomplete({
			delay: 0,
			source: data
		});
		
		});
		
		if("${payload.action.name}" == "exceptionThresholdUpdate") {
			$('#smsSending').val("${model.exceptionLimit.smsSending}");
		}
	</script>

<form name="exceptionConfig" id="form" method="post"
	action="${model.pageUri}?op=exceptionThresholdUpdateSubmit&type=异常阈值">
	<h4 class="text-center text-danger" id="state">&nbsp;</h4>
	<h4 class="text-center text-danger">修改异常报警配置信息</h4>
	<table class="table table-striped table-condensed   table-hover">
	<c:set  var="action" value="exceptionThresholdUpdate"/>
		<tr>
			<td style="text-align:right" class="text-success" width="20%">项目名称</td>
			<td>
			<c:choose>
			<c:when test="${payload.action.name eq action}">
				<input name="exceptionLimit.domain" value="${model.exceptionLimit.domain}" readonly required/>
			</c:when>
			<c:otherwise>
				<div class="navbar-header pull-left position" >
					<div class="input-group">
					<input name="exceptionLimit.domain" id="search_domain" size="60" type="text" class="search-input form-control ui-autocomplete-input" size="30" placeholder="input domain for search" autocomplete="off"/>
					</div>
				</div>
			</c:otherwise>
			</c:choose>
			</td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success" width="20%">异常名称</td>
			<td>
			<c:choose>
			<c:when test="${payload.action.name eq action}">
				<input name="exceptionLimit.id" value="${model.exceptionLimit.id}" readonly required/>
			</c:when>
			<c:otherwise>
				<div class="navbar-header pull-left position">
					<div class="input-group">
					<input name="exceptionLimit.id" id="search_exception" size="60" type="text" class="search-input form-control ui-autocomplete-input" size="30" placeholder="input exception for search" autocomplete="off"/>
					</div>
				</div>
			</c:otherwise>
			</c:choose>
		 
			</td>
		</tr>
		
		<tr>
			<td style="text-align:right" class="text-success" width="20%">短信告警</td>
			<td>
				<select name="exceptionLimit.smsSending" id="smsSending" style="width:200px;">
                	<option value="true">是</option>
                	<option value="false">否</option> 							
            	</select>
			</td>
		</tr>
		
		<tr>
			<td style="text-align: right" class="text-success" width="20%">warning阈值</td>
			<td><input id="warningThreshold" name="exceptionLimit.warning"
				value="${model.exceptionLimit.warning}" required /></td>
		</tr>
		
		<tr>
			<td style="text-align: right" class="text-success" width="20%">error阈值</td>
			<td><input id="errorThreshold" name="exceptionLimit.error"
				value="${model.exceptionLimit.error}" required /></td>
		</tr>
		<tr>
			<td colspan='2'  style="text-align:center"><input class='btn btn-primary' id="addOrUpdateExceptionConfigSubmit" type="submit"
				name="submit" value="提交"/></td>
		</tr>
	</table>
</form>
</a:config>