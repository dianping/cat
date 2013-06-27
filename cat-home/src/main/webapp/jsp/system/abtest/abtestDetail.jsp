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
</style>
<a:body>
   <script src="http://code.jquery.com/jquery-1.8.3.js"></script>
   <res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
   <res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
   <res:useCss value="${res.css.local['select2.css']}" target="head-css" />
   <res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['abtestAllTest.js']}" target="head-js" />
   <res:useJs value="${res.js.local['bootstrap-validation.min.js']}" target="head-js" />
   <div id="content" class="row-fluid">
      <div class="span12 column">
         <h3>
            Detail <small>${model.abtest.name} #${model.abtest.id}</small>
         </h3>
         <ul class="nav nav-tabs">
            <li><a href="?op=report&id=${payload.id }"> <img style="vertical-align: text-bottom;" height="15" width="15"
                  src="${res.img.local['star_black_small.png']}"> Summary
            </a></li>
            <li><a href="#detail"> <img style="vertical-align: text-bottom;" height="15" width="15"
                  src="${res.img.local['details_black_small.png']}"> Detail Report
            </a></li>
            <li class="active"><a href="?op=detail&id=${payload.id }"> <img style="vertical-align: text-bottom;"
                  height="15" width="15" src="${res.img.local['settings_black_small.png']}"> View/ Edit ABTest Details
            </a></li>
         </ul>
      </div>
   </div>

   <div style="width: 950px; margin: 0 auto; margin-bottom: 250px;">
      <c:choose>
         <c:when test="${ctx.exception != null}">
            <div id="errorMsg" style="margin-left: 170px; margin-top:5px; padding: 0; width: 300px;">
               <div style="position: absolute; width: 400px;" class="alert alert-error">
                  <button type="button" class="close" data-dismiss="alert">×</button>
                  <span style="text-align: center;">${ctx.exception.message}</span>
               </div>
            </div>
         </c:when>
         <c:when test="${ctx.httpServletRequest.method == 'POST'}">
            <div id="successMsg" style="margin-left: 170px; margin-top:5px; padding: 0; width: 300px;">
               <div style="position: absolute; width: 400px;" class="alert alert-success">
                  <button type="button" class="close" data-dismiss="alert">×</button>
                  <span style="text-align: center;">Modify successful! </span>
               </div>
            </div>
         </c:when>
      </c:choose>
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
                  <label class="control-label">Case Id <i tips="" data-trigger="hover" class="icon-question-sign"
                     data-toggle="popover" data-placement="top" data-original-title="tips"
                     data-content="It's important, because your client's code should use this 'Case Id' to specify a ABTest Case"></i>
                  </label>
                  <div class="controls" style="margin-top:4px;">
                     <strong style="font-size:16px">${model.abtest.caseId}</strong>
                  </div>
               </div>
            </c:if>
            <div class="control-group">
               <label class="control-label">AB Test Name <i tips="" data-trigger="hover" class="icon-question-sign"
                  data-toggle="popover" data-placement="top" data-original-title="tips"
                  data-content="Only charactor, number and underline are allowed. e.g. CatWeb_1"></i>
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
                     <input name="startDate" readonly="readonly" value="${w:format(model.abtest.startDate,'yyyy-MM-dd HH:mm')}"
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
                     <input name="endDate" readonly="readonly" value="${w:format(model.abtest.endDate,'yyyy-MM-dd HH:mm')}"
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
            <h5>Group Strategy</h5>
            <hr style="margin-top: 5px;">
            <div class="control-group">
               <label class="control-label">Strategy Name</label>
               <div class="controls">
                  <select name="strategyId" check-type="required" required-message="Strategy is required!" disabled="disabled">
                     <c:forEach var="item" items="${model.groupStrategyList}">
                        <option value="${item.id }" <c:if test="${item.id == model.abtest.groupStrategy}">selected="selected"</c:if>>${item.alias
                           }</option>
                     </c:forEach>
                  </select>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">Strategy Configuration</label>
               <div class="controls">
                  <textarea id="txtStrategyConfig" name="strategyConfig" class="span6" rows="3" cols="60" readonly="readonly">${model.abtest.strategyConfiguration}</textarea>
               </div>
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

				var changed = false;

				function enableEdit() {
					//input
					$('#form input').removeAttr("readonly");
					$('#txtStrategyConfig').removeAttr("readonly");
					$('#inputName').attr("readonly", "readonly");
					//$('#form select').removeAttr("disabled");
					$("#domains").select2("enable");
					$("#datetimepicker1>span").removeClass('hide');
					$("#datetimepicker2>span").removeClass('hide');
					//button
					$("#submit").show();
					$("#cancel").show();
					$("#edit").hide();
				}

				function disableEdit() {
					if (changed) {
						$("#cancleAffirmModal").modal('show');
					} else {
						$('#form input').attr("readonly", "readonly");
						$('#form select').attr("disabled", "disabled");
						$("#domains").select2("disable");
						$("#datetimepicker1>span").addClass('hide');
						$("#datetimepicker2>span").addClass('hide');
						//button
						$("#submit").hide();
						$("#cancel").hide();
						$("#edit").show();
					}
				}
				
				var timeout = 1;
				function countDown() {
					$('#countDown').text(timeout);
					timeout--;
					if (timeout == 0) {
						window.location.href = "abtest?op=list";
					} else {
						setTimeout("countDown()", 1000);
					}
				}

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
					$("#domains")
							.select2(
									{
										placeholder : "select which domains to run this ab test",
										allowClear : true
									});
					$("#domains").val(initDomains).trigger("change");
					$("#domains").select2("disable");
					//tips
					$('i[tips]').popover();
					//validate
					$('#form').validation();
					//onchange
					$("#form input, #form textarea,#form select").change(
							function() {
								changed = true;
							});
				});
			</script>
</a:body>