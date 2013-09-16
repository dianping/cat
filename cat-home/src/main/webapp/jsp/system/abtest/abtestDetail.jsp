<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model" scope="request" />
<style>
#content {
	width: 1200px;
	margin: 0 auto;
}

div.controls input {
	height: 30px;
}

#form input[disabled],#form  select[disabled],#form  textarea[disabled],#form  input[readonly],#form  select[readonly],#form  textarea[readonly]
	{
	background-color: #F7F7F9;
	cursor: text;
}

.inline-space{
	margin-bottom: 5px;
}

</style>
<a:body>
   <res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
   <res:useCss value="${res.css.local['select2.css']}" target="head-css" />
   <res:useCss value="${res.css.local['slider.css']}" target="head-css" />
   <res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['abtestAllTest.js']}" target="head-js" />
   <res:useJs value="${res.js.local['bootstrap-validation.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['bootstrap-slider.js']}" target="head-js" />
   <div id="content" class="row-fluid">
      <div class="span12 column">
         <h3>
            Detail <small>${model.abtest.name} #${model.abtest.id}</small>
         </h3>
         <ul class="nav nav-tabs">
            <li><a href="?op=report&id=${payload.id }"> <img style="vertical-align: text-bottom;" height="15" width="15"
    <%--               src="${res.img.local['star_black_small.png']}"> Summary
            </a></li>
            <li><a href="#detail"> <img style="vertical-align: text-bottom;" height="15" width="15" --%>
                  src="${res.img.local['details_black_small.png']}"> Detail Report
            </a></li>
            <li class="active"><a href="?op=detail&id=${payload.id }"> <img style="vertical-align: text-bottom;"
                  height="15" width="15" src="${res.img.local['settings_black_small.png']}"> View/ Edit ABTest Details
            </a></li>
         </ul>
      </div>
   </div>

   <div style="width: 950px; margin: 0 auto; margin-bottom: 250px;">
      <div id="alertDiv" style="margin-left: 170px; margin-top:5px;padding: 0; width: 300px;"></div>
      <div style="width: 90%;">
         <form id="form" method="post" action="" class="form-horizontal">
            <button id="cancel" type="button" onclick="disableEdit()" style="float: right; margin-left: 20px" class="btn hide">cancel</button>
            <button id="submit" style="float: right;" type="submit" class="btn btn-success hide">submit</button>
            <button id="edit" style="float: right;" type="button" onclick="enableEdit()" class="btn btn-info">Edit</button>
            <h5>Basic Information</h5>
            <hr style="margin-top: 20px;">
            <input type="hidden" name="id" value="${model.abtest.id}"> <input type="hidden" name="op" value="detail">
            <c:if test="${model.abtest.caseId != null}">
               <div class="control-group">
                  <label class="control-label">ID <i class="icon-question-sign"></i>
                  </label>
                  <div class="controls" style="margin-top:4px;">
                     <strong style="font-size:16px">${model.abtest.run.id}</strong>
                  </div>
               </div>
            </c:if>
            <div class="control-group">
               <label class="control-label">AB Test Name <i tips="" data-trigger="hover" class="icon-question-sign"
                  data-toggle="popover" data-placement="top" data-original-title="tips"
                  data-content="Only charactor, number and underline are allowed. e.g. CatWeb_1.It's important, because your client's code should use this 'Name' to specify a ABTest Case"></i>
               </label>
               <div class="controls">
                  <input id="inputName" type="text" name="name" placeholder="give it a name ..." check-type="required"
                     required-message="Name is required!" value="${model.abtest.name}" readonly="readonly">
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">Description</label>
               <div class="controls">
                  <textarea readonly="readonly" name="description" placeholder="say something about the abtest ... " class="span6"
                     rows="3" cols="60">${model.abtest.description}</textarea>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">Start Time</label>
               <div class="controls">
                  <div id="datetimepicker1" class="input-append date">
                     <input name="startDate" readonly="readonly" value="${w:format(model.abtest.run.startDate,'yyyy-MM-dd hh:mm')}"
                        placeholder="when to run ab test" data-format="yyyy-MM-dd hh:mm" type="text" check-type="required"
                        required-message="Start Time is required!"></input> <span class="add-on hide"> <i
                        data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
                     </span>
                  </div>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">End Time</label>
               <div class="controls">
                  <div id="datetimepicker2" class="input-append date">
                     <input name="endDate" readonly="readonly" value="${w:format(model.abtest.run.endDate,'yyyy-MM-dd hh:mm')}"
                        placeholder="when to stop ab test" data-format="yyyy-MM-dd hh:mm" type="text" check-type="required"
                        required-message="End Time is required!"></input> <span class="add-on hide"> <i
                        data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
                     </span>
                  </div>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">Domain <i tips="" data-trigger="hover" class="icon-question-sign"
                  data-toggle="popover" data-placement="top" data-original-title="tips"
                  data-content="you can choose one or more than one domain"></i>
               </label>
               <div class="controls">
                  <select multiple="" name="domains" id="domains" style="width: 350px;" class="populate select2-offscreen"
                     tabindex="-1" check-type="required" required-message="End Time is required!">
                     <c:forEach var="item" items="${model.projectMap}">
                        <optgroup label="${item.key}">
                           <c:forEach var="listItem" items="${item.value}">
                              <option value="${listItem.domain}">${listItem.domain}</option>
                           </c:forEach>
                        </optgroup>
                     </c:forEach>
                  </select>
               </div>
            </div>
            <h5>Traffic Filter</h5>
			<hr style="margin-top: 20px;">
			<div class="control-group">
				<label class="control-label">Test Page URL</label>
				<div class="controls">
					<input id="input1" type="url" name="1" placeholder="http://www.example.com" check-type="required" required-message="URL is required!"  class="input-xlarge" readonly="readonly">
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Test Page Pattern</label>
				<div class="controls">
					<span  id="forInput2">No Pattern specified</span>
					<input  id="input2" type="url" name="5" placeholder="http://www.example.com/*" class="input-xlarge hide" readonly="readonly">
					<a class="pull-right active hide" href="javascript:void(0);" id="edit0">Edit</a> 
					<a class="pull-right active hide" href="javascript:void(0);" id="save0">Save</a> 
					<a class="pull-right active hide" href="javascript:void(0);" id="cancel0">Cancel &nbsp;</a> 
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Exclude URLs</label>
				<div class="controls">
					<span id="forInput3">No URL excluded</span>
					<input id="input3" type="url" name="2" placeholder="http://www.example.com/1" class="input-xlarge hide" readonly="readonly">
					<a class="pull-right active hide" href="javascript:void(0);" id="edit1">Edit</a> 
					<a class="pull-right active hide" href="javascript:void(0);" id="save1">Save</a> 
					<a class="pull-right active hide" href="javascript:void(0);" id="cancel1">Cancel &nbsp;</a> 
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Traffic Segment</label>
				<div class="controls">
					<span id="input4">All visitors</span>
					<div id="div2" class="hide">
						<table><tbody></tbody></table>
					</div>
					<a class="pull-right active hide" href="javascript:void(0);" id="edit2">Edit</a> 
					<a class="pull-right active hide" href="javascript:void(0);" id="save2">Save</a> 
					<a href="javascript:void(0)" id="addVistorCondition" class="pull-right active hide"><i class="icon-plus"></i>Add visitor condition &nbsp;</a>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Traffic Included in Test</label>
				<div class="controls">
						<input class="slider" type="text" class="span2" value="" data-slider-min="0" data-slider-max="100" data-slider-step="1" data-slider-value="100" data-slider-orientation="horizontal" data-slider-selection="after" data-slider-tooltip="show" readonly="readonly">
					<span class="add-on hide">&nbsp;&nbsp;</span>
					<span class="add-on">100</span>
					<span class="add-on">%</span>
					<a class="pull-right active hide" href="javascript:void(0);" id="edit3">Edit</a> 
					<a class="pull-right active hide" href="javascript:void(0);" id="save3">Save</a> 
					<a class="pull-right active hide" href="javascript:void(0);" id="cancel3">Cancel &nbsp;</a> 
				</div>
			</div>
			
			<h5>Conversion Goals</h5>
			<hr style="margin-top: 20px;">
			<div class="control-group">
				<label class="control-label">Convertion Goals</label>
				<div class="controls">
					<div id="div3">
					
					</div>
					<a href="javascript:void(0)" id="addConvertionGoal" class="pull-right active"><i class="icon-plus"></i>Add another goal</a>
				</div>
			</div>
			
            <h5>Group Strategy</h5>
            <hr style="margin-top: 5px;">
            <div class="control-group">
               <label class="control-label">Strategy Name</label>
               <div class="controls">
                  <select id="strategyId" name="strategyId" check-type="required" required-message="Strategy is required!" disabled="disabled">
                     <c:forEach var="item" items="${model.groupStrategyList}">
                     	<option value="0">请选择一个分组策略</option>
                        <option value="${item.id }" <c:if test="${item.id == model.abtest.groupStrategy}">selected="selected"</c:if>>${item.name}</option>
                     </c:forEach>
                  </select>
               </div>
            </div>
            <div id="groupStrategyDivsub">
			</div>
         </form>
      </div>
   </div>
   <!-- 错误消息弹出框 -->
   <div aria-hidden="true" aria-labelledby="myModalLabel" role="dialog" tabindex="-1" class="modal hide fade" id="errorMsgModal"
      style="display: none;">
      <div class="modal-header">
         <button aria-hidden="true" data-dismiss="modal" class="close" type="button">×</button>
         <h3></h3>
      </div>
      <div class="modal-body">
         <p style="text-align: center"></p>
      </div>
      <div class="modal-footer">
         <button data-dismiss="modal" class="btn btn-info">关闭</button>
      </div>
   </div>
   <!-- 取消或离开的确认框 -->
   <div aria-hidden="true" data-backdrop="true" role="dialog" tabindex="-1" class="modal hide" id="cancleAffirmModal"
      style="display: none;">
      <div class="modal-header">
         <button aria-hidden="true" data-dismiss="modal" class="close" type="button">×</button>
         <h3>请确认</h3>
      </div>
      <div class="modal-body">
         <p style="text-align: center;">
            <i class="icon-warning-sign"></i> 您已经做了修改操作，是否确定不保存？
         </p>
      </div>
      <div class="modal-footer">
         <button onclick="window.location = window.location.href;" class="btn btn-primary">确定</button>
         <button data-dismiss="modal" class="btn">取消</button>
      </div>
   </div>
   <script type="text/template" id="alert_success">
         <div style="position: absolute; width: 200px;" class="alert alert-success">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <span style="text-align: center;"></span>
         </div>
</script>
   <script type="text/template" id="alert_error">
         <div style="position: absolute; width: 200px;" class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <span style="text-align: center;"></span>
         </div>
</script>

   <script>
				var initDomains = [];
				<c:forEach var="domain" items="${model.abtest.domains}">
				initDomains.push("${domain}");
				</c:forEach>
				
				var comparators = ["is equal to (case insens.)","is not equal to (case insens.)",
				                  "is equal to (case sens.)","is not equal to (case sens.)",
				                  "marches Regex (case insens.)","marches Regex (case sens.)",
				                  "contains","does not contains"];

				var changed = false;

				function enableEdit() {
					//input
					$('#form input').removeAttr("readonly");
					$('#groupStrategyDivsub textarea').removeAttr("readonly");
					$('#inputName').attr("readonly", "readonly");
					//$('#form select').removeAttr("disabled");
					$("#domains").select2("enable");
					$("#datetimepicker1>span").removeClass('hide');
					$("#datetimepicker2>span").removeClass('hide');
					//button
					$("#submit").show();
					$("#cancel").show();
					$("#edit").hide();
					
					$("#edit0").toggleClass("hide");
					$("#edit1").toggleClass("hide");
					$("#edit2").toggleClass("hide");
					$("#edit3").toggleClass("hide");
				}

				function disableEdit() {
					if (changed) {
						$("#cancleAffirmModal").modal('show');
					} else {
						$('#form input').attr("readonly", "readonly");
						$('#groupStrategyDivsub textarea').attr("readonly", "readonly");
						$('#form select').attr("disabled", "disabled");
						$("#domains").select2("disable");
						$("#datetimepicker1>span").addClass('hide');
						$("#datetimepicker2>span").addClass('hide');
						//button
						$("#submit").hide();
						$("#cancel").hide();
						$("#edit").show();
						
						$("#edit0").toggleClass("hide");
						$("#edit1").toggleClass("hide");
						$("#edit2").toggleClass("hide");
						$("#edit3").toggleClass("hide");
					}
				}
				
				$("#edit0,#save0,#cancel0").click(function(e){
					var parent = $(this).parent();
					$("span",parent).toggleClass("hide");
					$("input",parent).toggleClass("hide");
					$("a",parent).toggleClass("hide");
					
					var id = $(this).attr("id");
					
					if(id == "save0"){
						if($("input",parent).val() != ""){
							$("span",parent).text($("input",parent).val());
						}else{
							$("span",parent).text("No Pattern specified");
						}
					}else if(id == "edit0"){
						if($("span",parent).text() != "No Pattern specified"){
							$("input",parent).val($("span",parent).text());
						}
					}
				});
				
				$("#edit1,#save1,#cancel1").click(function(e){
					var parent = $(this).parent();
					$("span",parent).toggleClass("hide");
					$("input",parent).toggleClass("hide");
					$("a",parent).toggleClass("hide");
					
					var id = $(this).attr("id");
					
					if(id == "save1"){
						if($("input",parent).val() != ""){
							$("span",parent).text($("input",parent).val());
						}else{
							$("span",parent).text("No URL excluded");
						}
					}else if(id == "edit1"){
						if($("span",parent).text() != "No URL excluded"){
							$("input",parent).val($("span",parent).text());
						}
					}
				});
				
				$("#edit2,#save2").click(function(e){
					var parent = $(this).parent();
					$("span",parent).toggleClass("hide");
					$("#addVistorCondition").toggleClass("hide");
					$("#edit2").toggleClass("hide");
					$("#save2").toggleClass("hide");
					$("#cancel2").toggleClass("hide");
					$("div:first",parent).toggleClass("hide");
					
					var id = $(this).attr("id");
					
					if(id == "save2"){
						var conditions = getConditionsHTML();
						
						if(conditions){
							$("span",parent).html(conditions);
						}else{
							$("span",parent).html("All visitors");
						}
					}else if(id == "edit2"){
					}
					
				});
				
				$("#edit3,#save3,#cancel3").click(function(e){
					var parent = $(this).parent();
					$("span:first",parent).toggleClass("hide");
					$("a",parent).toggleClass("hide");
					$('.slider').toggleClass('hide');
				});
				
				function initConf(slider){
					var jsonObject = ${model.abtest.strategyConfiguration};
					var conditions = ${model.abtest.conditions};
					var goals = ${model.abtest.conversionGoals};
					
					//console.log(conditions);
					//console.log(jsonObject);
					var innerHTML = "";
					
					for(var i in jsonObject['fields']){
						var field = jsonObject['fields'][i];
						
						if(field['inputType'] == "textarea"){
							innerHTML += '<div class="control-group"><label class="control-label">' + field['name'] + '</label>'
			                         + '<div class="controls"><textarea readonly class="span6" rows="3" cols="60" name="' + field['name'] + '">' + field['value'] + '</textarea></div>'
									 + '</div>';
						}else if(field['inputType'] == "input"){
							innerHTML += '<div class="control-group"><label class="control-label">' + field['name'] + '</label>'
							         + '<div class="controls"><input type="text" readonly name="' + field['name'] + '" value="' + field['value'] + '"></div>'
						             + '</div>';
						}
					}
					
					for(var i = 0 ; i < conditions.length; i++){
						var condition = conditions[i];
						var seq = condition['seq'];
						var text = condition["text"];
						
						if(seq == 0){
							$("#input1").val(text);
						}else if(seq == 1){
							$("#forInput2").text(text);
							$("#input2").val(text);
						}else if(seq == 2){
							$("#forInput3").text(text);
							$("#input3").val(text);
						}else if(seq == 3){
							var html = "";
							var lastOp = "";
							while(true){
								var tmp = conditions[i+1];
								var index = parseInt(condition['comparator']) - 1;
								
								$('#addVistorCondition').trigger('click');
								//console.log(condition['comparator']);
								$('#div2 tr:last select:eq(0)').val(condition['name']);
								$('#div2 tr:last select:eq(1)').val(condition['comparator']);
								$('#div2 tr:last input').val(condition['text']);
								
								if(lastOp != ""){
									if(lastOp == "or"){
										$("a",$('#div2 tr:last').prev()).toggleClass("btn-primary");
									}
								}
								
								lastOp = condition["operator"];
								
								if(tmp && tmp['seq'] == 3){
									var op = "AND";
									
									if(condition['operator'] == "or"){
										op = "OR";
									}
									
									html += "<p><strong>" + condition['name'] + " " + comparators[index] + " " + condition['text'] + " <span class='label label-info'>" + op + "</span><strong></p>";
									
								}else if(tmp && tmp['seq'] != 3){
									html += "<p><strong>" + condition['name'] + " " + comparators[index] + " " + condition['text'] + " <strong></p>";
									$('#input4').html(html);
									
									break;
								}
								
								condition = tmp;
								i++;
							}
						}else if(seq == 4){
							var value = condition['text'];
							
							$(".add-on:eq(3)").text(value);
							slider.slider('setValue', value)
						}
					}
					
					//alert(innerHTML);
					$('#groupStrategyDivsub').empty();
					$('#groupStrategyDivsub').html(innerHTML);
					
					for(var i in goals){
						var goal = goals[i];
						
						$('#addConvertionGoal').trigger('click');
						$('#div3 div:last input:eq(0)').val(goal["name"]);
						$('#div3 div:last input:eq(0)').attr("readonly","readonly");
						$('#div3 div:last input:eq(1)').val(goal["text"]);
						$('#div3 div:last input:eq(1)').attr("readonly","readonly");
					}
				}

				$("#form" ).on( "submit", function( event ) {
					event.preventDefault();
					
					var params = $(this).serialize();
					//console.log(params);
					var jsonObject = ${model.abtest.strategyConfiguration};
					
					for(var i in jsonObject['fields']){
						var field = jsonObject['fields'][i];
						var name = field["name"];
						var type = field["inputType"];
						
						field["value"] = $(type + '[name=' + name + ']').val();
					}

					params += "&strategyConfig=" + JSON.stringify(jsonObject);
					
					var conditions = getConditions();
					params += "&conditions=" + JSON.stringify(conditions);
					
					var goals = getConvertionGoals();
					params += "&goals=" + JSON.stringify(goals);
					//console.log(params);
					$.ajax({
						type: "POST",
						url : "abtest?op=ajax_detail",
						data: params
					}).done(function(json) {
						json = JSON.parse(json);
						
						if(json.code == 0){
							var innerHTML = '<div style="position: absolute; width: 400px;" class="alert alert-success">'
									+ '<button type="button" class="close" data-dismiss="alert">×</button>'
									+ '<span style="text-align: center;">' + json.msg + '</span></div>';
							$('#alertDiv').html(innerHTML);
							changed = false;
							disableEdit();
						}else if(json.code == 1){
							var innerHTML = '<div style="position: absolute; width: 400px;" class="alert alert-error">'
									+ '<button type="button" class="close" data-dismiss="alert">×</button>'
									+ '<span style="text-align: center;">' + json.msg + '</span></div>';
							$('#alertDiv').html(innerHTML);
						}
					});
				});
				
				$(function() {
					$('#datetimepicker1').datetimepicker({});
					$('#datetimepicker2').datetimepicker({});
					$('#datetimepicker1').on('changeDate', function(e) {
						changed = true;
					});
					$('#datetimepicker2').on('changeDate', function(e) {
						changed = true;
					});
					//domain selector
					$("#domains").select2({
						placeholder : "select which domains to run this ab test",
						allowClear : true
					});
					
					$("#domains").val(initDomains).trigger("change");
					$("#domains").select2("disable");
					
					var showData = function(){
						$(".add-on:eq(3)").text(data.getValue());
					}
					var slider = $('.slider').slider();
					var data = slider.on('slide',showData).data('slider');
					
					$('.slider').toggleClass("hide");
					
					initConf(slider);
					//tips
					$('i[tips]').popover();
					//validate
					//$('#form').validation();
					//onchange
					$("#form input, #form textarea,#form select").change(
						function() {
							changed = true;
						});
					});
			</script>
</a:body>