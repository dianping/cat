<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.business.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.business.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.business.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useCss value="${res.css.local['multiple-select.css']}" target="head-css" />
	<res:useJs value="${res.js.local['jquery.multiple.select.js']}" target="head-js" />
	<script type="text/javascript">

		$(document).ready(
			function() {
				$('#application_config').addClass('active open');
				$('#businessConfig').addClass('active');

				var domain = '${payload.domain}';
				if (domain != null && domain.length != 0) {
					$("#domain").val(domain);
				}
				//custom autocomplete (category selection)
				$.widget("custom.catcomplete", $.ui.autocomplete, {
					_renderMenu : function(ul, items) {
						var that = this, currentCategory = "";
						$.each(items, function(index, item) {
							that._renderItemData(ul, item);
						});
					}
				});

				var data = [];
				<c:forEach var="item" items="${model.domains}">
				var item = {};
				item['label'] = '${item}';
				data.push(item);
				</c:forEach>

				$("#domain").catcomplete({
					delay : 0,
					source : data
				});
				
				var action = '${payload.action.name}';

				if (action == 'addSubmit'	|| action == 'alertRuleAddSubmit' ||
					action == 'customAddSubmit' || action == 'customDelete' || action == 'delete') {
					var state = '${model.opState}';
					if (state == 'Success') {
						$('#state').html('操作成功');
					} else {
						$('#state').html('操作失败');
					}
					setInterval(function() {
						$('#state').html('&nbsp;');
					}, 3000);
				}
			
				$('#wrap_search').submit(
						function(){
							query();
							return false;
						}		
					);
			});

		function query() {
			var domain = $("#domain").val();
			var href = "?op=list&domain=" + domain;
			window.location.href = href;
		}
	</script>
	<div>
		<form id="wrap_search" >
		<table align="center">
			<tr>
				<th>
					<div class="input-group" style="float: left;">
						<span class="input-group-addon">Domain</span> <span
							class="input-icon" style="width: 250px;"> <input
							type="text" placeholder=""
							class="search-input search-input form-control ui-autocomplete-input"
							id="domain" autocomplete="on" data="" /> <i
							class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</div> <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" />
				</th>
			</tr>
		</table>
		</form>
	    <h4 class="text-center text-danger">业务大盘标签会默认进行基线告警</h4>
	    <h4 class="text-center text-danger" id="state">&nbsp;</h4>
     	<table class="table table-striped table-condensed table-bordered table-hover">
     		<tr class="text-success">
     			<th width="9%"><h5 class='text-center'>项目</h5></th>
     			<th width="4%"><h5 class='text-center'>显示顺序</h5></th>
     			<th width="4%"><h5 class='text-center'>敏感数据</h5></th>
     			<th width="4%"><h5 class='text-center'>是否告警</h5></th>
     			<th width="12%"><h5 class='text-center'>BusinessKey</h5></th>
     			<th width="16%"><h5 class='text-center'>标题</h5></th>
     			<th width="18%"><h5 class='text-center'>标签</h5></th>
     			<th width="9%"><h5 class='text-center'>次数</h5></th>
     			<th width="9%"><h5 class='text-center'>平均值</h5></th>
     			<th width="9%"><h5 class='text-center'>总和</h5></th>
     			<th width="13%"><h5 class='text-center'>操作
				&nbsp;&nbsp;<a class="btn update btn-primary btn-xs" href="?op=customAdd&domain=${payload.domain}">新增</a>
					</h5></th>
     		</tr>
	     	<c:forEach var="config" items="${model.configs}">
     			<tr>
     			<td>${payload.domain}</td>
     			<td>${config.viewOrder}</td>
     			<td>
     				<c:if test="${config.privilege}">
     					<span class="text-danger">是</span>
     				</c:if>
     				<c:if test="${config.privilege == false}">
     					<span>否</span>
     				</c:if>
     			</td>
     			<td>
     				<c:if test="${config.alarm}">
     					<span class="text-danger">是</span>
     				</c:if>
     				<c:if test="${config.alarm == false}">
     					<span>否</span>
     				</c:if>
     			</td>
     			<td style="word-wrap:break-word;word-break:break-all;">${config.id}</td>
     			<td style="word-wrap:break-word;word-break:break-all;">${config.title}</td>
     			<td>
     				<c:if test="${model.tags[config.id] != null}">
     					<c:forEach var="tag" items="${model.tags[config.id]}">
     						<span class="label label-info">${tag}</span> &nbsp;
     					</c:forEach>
     				</c:if>
     			</td> 
     			<td align='right'>
     				<c:if test="${config.showCount}">
     					<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
     				</c:if>&nbsp;&nbsp;&nbsp;&nbsp;
     				<a href="?op=alertRuleAdd&key=${config.id}&domain=${payload.domain}&attributes=COUNT" id="alertRule" class="btn btn-primary btn-xs">告警</a>
     			</td>
     			<td align='right'>
     				<c:if test="${config.showAvg}">
     					<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
     				</c:if>&nbsp;&nbsp;&nbsp;&nbsp;
	     			<a href="?op=alertRuleAdd&key=${config.id}&domain=${payload.domain}&attributes=AVG" id="alertRule" class="btn btn-primary btn-xs">告警</a>
     			</td>
     			<td align='right'>
     				<c:if test="${config.showSum}">
     					<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
     				</c:if>&nbsp;&nbsp;&nbsp;&nbsp;
	     			<a href="?op=alertRuleAdd&key=${config.id}&domain=${payload.domain}&attributes=SUM" id="alertRule" class="btn btn-primary btn-xs">告警</a>
     			</td>
	     		<td style="text-align:center;white-space: nowrap">
		      		<a href="?op=add&key=${config.id}&domain=${payload.domain}" class="btn btn-primary btn-xs">
				<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
					<a href="?op=delete&key=${config.id}&domain=${payload.domain}" class="btn btn-danger btn-xs delete" >
				<i class="ace-icon fa fa-trash-o bigger-120"></i></a>
 	     		</td>
	     		</tr>
	     	</c:forEach>
	     	<c:forEach var="config" items="${model.customConfigs}">
     			<tr>
     			<td>${payload.domain}</td>
     			<td>${config.viewOrder}</td>
     			<td>
     				<c:if test="${config.privilege}">
     					<span class="text-danger">是</span>
     				</c:if>
     				<c:if test="${config.privilege == false}">
     					<span>否</span>
     				</c:if>
     			</td>
     			<td>
					<c:if test="${config.alarm}">
     					<span class="text-danger">是</span>
     				</c:if>
     				<c:if test="${config.alarm == false}">
     					<span>否</span>
     				</c:if>     			
     			</td>
     			<td style="word-wrap:break-word;word-break:break-all;">${config.id}</td>
     			<td style="word-wrap:break-word;word-break:break-all;">${config.title}</td>
     			<td>
     				<c:if test="${model.tags[config.id] != null}">
     					<c:forEach var="tag" items="${model.tags[config.id]}">
     						<span class="label label-info">${tag}</span> &nbsp;
     					</c:forEach>
     				</c:if>
     			</td> 
     			<td align='center'></td>
     			<td align='center'>
     				<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
     				<a href="?op=alertRuleAdd&key=${config.id}&domain=${payload.domain}&attributes=AVG" id="alertRule" class="btn btn-primary btn-xs">告警</a>
     			<td align='center'>
     			</td>
	     		<td style="text-align:center;white-space: nowrap">
		      		<a href="?op=customAdd&key=${config.id}&domain=${payload.domain}" class="btn btn-primary btn-xs">
				<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
					<a href="?op=customDelete&key=${config.id}&domain=${payload.domain}" class="btn btn-danger btn-xs delete" >
				<i class="ace-icon fa fa-trash-o bigger-120"></i></a>
 	     		</td>
	     		</tr>
	     	</c:forEach>
     	</table>
     </div>
</a:config>
