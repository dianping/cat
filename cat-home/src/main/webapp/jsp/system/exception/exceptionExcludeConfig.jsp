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
		$('#alert_config').addClass('active open');
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
				
		$("#search_exception").catcomplete({
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
				
		$("#search_domain").catcomplete({
			delay: 0,
			source: data
		});
		
		});

		function setWidth(){
			var sel =  document.getElementById("domainId");
			sel.style.width = ((sel.offsetWidth < 200) ? '200' : 'auto');
		}
</script>

<form name="exceptionConfig" id="form" method="post"
	action="${model.pageUri}?op=exceptionExcludeUpdateSubmit&domain=${payload.domain}&exception=${payload.exception}&type=exclude">
	<h4 class="text-center text-danger" id="state">&nbsp;</h4>
	<h4 class="text-center text-danger">修改异常报警配置信息</h4>
	<table class="table table-striped table-condensed   table-hover">
		<tr>
			<td style="text-align:right" class="text-success" width="20%">项目名称</td>
			<td>
			<div class="navbar-header pull-left position" >
				<div class="input-group">
				<input name="exceptionExclude.domain" id="search_domain" size="60" type="text" class="search-input form-control ui-autocomplete-input" size="30" placeholder="input domain for search" autocomplete="off" required />
				</div>
			</div><span class="text-danger">&nbsp;&nbsp;*</span>
			</td>
		</tr>

		<tr>
			<td style="text-align:right" class="text-success" width="20%">异常名称</td>
			<td width="80%">
			<div class="navbar-header pull-left position">
				<div class="input-group">
				<input name="exceptionExclude.name" id="search_exception" size="60" type="text" class="search-input form-control ui-autocomplete-input" size="30" placeholder="input exception for search" autocomplete="off" required />
				</div>
			</div><span class="text-danger">&nbsp;&nbsp;*</span>
			</td>
		</tr>
		<tr>
			<td colspan='2' ><input class='btn btn-primary' style="margin-left:30%" id="addOrUpdateExceptionConfigSubmit" type="submit"
				name="submit" value="提交"/></td>
		</tr>
	</table>
</form>
</a:config>