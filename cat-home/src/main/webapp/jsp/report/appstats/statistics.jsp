<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.appstats.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.appstats.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.appstats.Model" scope="request" />

<a:mobile>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<script src="${model.webapp}/assets/js/bootstrap-tag.min.js"></script>
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
		<div id="queryBar">
		<table>
		<tr>
	        <td>
	        <div style="float:left;">
	        &nbsp;&nbsp;App&nbsp;&nbsp;<select id="app">
	        <c:forEach var="item" items="${model.constantsItem.items}">
	        	<option value="${item.key}">${item.value.value}</option>
	        </c:forEach>
	        </select>&nbsp;&nbsp;日期
			<input type="text" id="time" style="width:100px;" value="<fmt:formatDate value='${payload.dayDate}' pattern='yyyy-MM-dd'/>"/>
			</div></td>
			<td>&nbsp;&nbsp;查询返回码分布</td>
			<td><div style="float:left;">
				<input type="text" name="codes" style="width:100px;" class="tag" id="tag_codes" placeholder="输入返回码，默认所有XXX" />
            </div></td>
    		<td>&nbsp;&nbsp;返回码分布TOP数</td>
			<td><div style="float:left;">
				<input type="text" name="top" style="width:50px;"  value="${payload.top}" id="top" placeholder="Enter top ... 默认20" />
            </div></td>
            <td>&nbsp;&nbsp;<input class="btn btn-primary  btn-sm"  value="查询" onclick="queryNew()" type="submit">
			</td>
			</tr></table>
		</div>
		<br>
		<%@ include file="statisticsDetail.jsp"%>
		<script type="text/javascript">
		  $(document).ready(function(){
			  $('[data-rel=tooltip]').tooltip();
			  $('#App_report').addClass("active open");
			  $('#statistics').addClass("active");
			  $('#time').datetimepicker({
					format:'Y-m-d',
					timepicker:false,
					maxDate:0
				});
			  
			  var type='${payload.type}';
			  if(type == 'request' || type.length==0){
				  type='all';
			  }
			  $("#li-"+type).addClass("active");
			  $("#tabContent-"+type).addClass("active");
			  
			  $('#app').val("${payload.appId}");
			  
			  var tag_input = $('#tag_codes');
				try{
					tag_input.tag(
					  {
						placeholder:tag_input.attr('placeholder'),
					  }
					)
					
					//programmatically add a new
					var $tag_obj = $('#tag_codes').data('tag');
					<c:forEach var="item" items="${payload.codes}" varStatus="status">
						$tag_obj.add("${item}");
					</c:forEach>
				}
				catch(e) {
					//display a textarea for old IE, because it doesn't support this plugin or another one I tried!
					tag_input.after('<textarea id="'+tag_input.attr('id')+'" name="'+tag_input.attr('name')+'" rows="3">'+tag_input.val()+'</textarea>').remove();
					//$('#form-field-tags').autosize({append: "\n"});
				}
			  
			 <c:forEach var="entry" items="${model.piecharts}" varStatus="status">
				graphPieChartWithName(document.getElementById('piechart_${entry.key}'), ${entry.value.jsonString},  '${entry.value.title}');
			</c:forEach>
	      });
	      
	      function queryNew(){
	      	var app=$('#app').val();
	        var time=$("#time").val();
	        var codes=$('#tag_codes').val();
	        var top=$('#top').val();
	        
	        window.location.href="?appId="+app+"&day="+time+"&domain=${model.domain}&type=${payload.type}&codes="+codes+"&top="+top;
	      }
		</script>
</a:mobile>