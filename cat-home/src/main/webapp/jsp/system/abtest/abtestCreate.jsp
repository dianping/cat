<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model" scope="request" />

<style>
	div.controls input {
		height: 30px;
	}
</style>

<a:body>
	<script src="${res.js.local['jquery-1.7.1.js']}"></script>
	<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
	<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['abtestAllTest.js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap-validation.min.js']}" target="head-js" />

	<div style="width: 950px; margin: 0 auto; margin-bottom: 250px;">
		<h4 style="margin: 0 auto;">Create ABTest</h4>
		<div id="alertDiv" style="margin-left: 170px; margin-top:5px;padding: 0; width: 300px;"></div>
		<div style="width: 90%;">
			<form id="form" method="post" action="abtest?op=addABTest" class="form-horizontal">
				<a href="abtest" style="float: right; margin-left: 20px" class="btn">Cancel</a>
				<button id="submit" style="float: right;" type="submit"
					class="btn btn-success">Submit</button>
				<h5>Basic Information</h5>
				<hr style="margin-top: 20px;">
				<div class="control-group">
					<label class="control-label">AB Test Name <i tips=""
						data-trigger="hover" class="icon-question-sign"
						data-toggle="popover" data-placement="top"
						data-original-title="tips"
						data-content="Only charactor, number and underline are allowed. e.g. CatWeb_1"></i>
					</label>
					<div class="controls">
						<input type="text" name="name" id="abName" placeholder="give it a name ..."
							check-type="required" required-message="Name is required!" value="${payload.name}">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">Owner <i tips=""
						data-trigger="hover" class="icon-question-sign"
						data-toggle="popover" data-placement="top"
						data-original-title="tips"
						data-content="Only charactor, number and underline are allowed. e.g. CatWeb_1"></i>
					</label>
					<div class="controls">
						<input type="text" name="owner" id="abOwn" placeholder="give it a owner ..."
							check-type="required" required-message="Owner is required!" value="${payload.owner}">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">Description</label>
					<div class="controls">
						<textarea name="description"
							placeholder="say something about the abtest ... " class="span6"
							rows="3" cols="60">${payload.description}</textarea>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">Start Time</label>
					<div class="controls">
						<div id="datetimepicker1" class="input-append date">
							<input name="startDate" value="${payload.startDateStr}"
								placeholder="default is current time."
								data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
								class="add-on"> <i data-time-icon="icon-time"
								data-date-icon="icon-calendar"> </i>
							</span>
						</div>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">End Time</label>
					<div class="controls">
						<div id="datetimepicker2" class="input-append date">
							<input name="endDate" value="${payload.endDateStr}"
								placeholder="default is forever" data-format="yyyy-MM-dd hh:mm"
								type="text"></input> <span class="add-on"> <i
								data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
							</span>
						</div>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">Domain <i tips=""
						data-trigger="hover" class="icon-question-sign"
						data-toggle="popover" data-placement="top"
						data-original-title="tips"
						data-content="you can choose one or more than one domain"></i>
					</label>
					<div class="controls">
						<select multiple="" name="domains" id="domains"
							style="width: 350px;" class="populate select2-offscreen"
							tabindex="-1" check-type="required"
							required-message="End Time is required!">
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
						<select id="strategyId" name="strategyId" check-type="required"
							required-message="Strategy is required!">
							<option value="0">请选择一个分组策略</option>
							<c:forEach var="item" items="${model.groupStrategyList}">
								<option value="${item.id}">${item.name }</option>
							</c:forEach>
						</select> <a href="#groupStrategyModal" role="button" class="btn" id="btnGroupStrategyModel"
							data-toggle="modal">Add</a>
					</div>
				</div>
				<div id="groupStrategyDivsub">
				
				</div>
			</form>
		</div>
	</div>

	<%@ include file="groupStrategy.jsp"%>

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
		<c:forEach var="domain" items="${payload.domains}">
		initDomains.push("${domain}");
		</c:forEach>
		
		var timeout = 3;
		function countDown() {
			$('#countDown').text(timeout);
			timeout--;
			if (timeout == 0) {
				window.location.href = "abtest";  //bug here
			} else {
				setTimeout("countDown()", 1000);
			}
		}
		
		$("#form" ).on( "submit", function( event ) {
			event.preventDefault();
			
			var abName = $('#abName').val();
			var abOwn = $('#abOwn').val();
			
			if(abName != "" && abOwn != ""){
				var params = $(this).serialize();
				//console.log(params);
				var jsonObject = $('#strategyId option[value=' + $('#strategyId').val() + ']').data();
				
				for(var i in jsonObject['fields']){
					var field = jsonObject['fields'][i];
					var name = field["name"];
					var type = field["inputType"];
					
					field["value"] = $(type + '[name=' + name + ']').val();
				}
	
				params += "&strategyConfig=" + JSON.stringify(jsonObject);
				//console.log(params);
				$.ajax({
					type: "POST",
					url : "abtest?op=ajax_addABTest",
					data: params,
					async:false
				}).done(function(json) {
					json = JSON.parse(json);
					
					if(json.code == 0){
						var innerHTML = '<div style="position: absolute; width: 400px;" class="alert alert-success">'
								+ '<button type="button" class="close" data-dismiss="alert">×</button>'
								+ '<span style="text-align: center;">Created! Going to the list page after '
								+ '<span id="countDown"></span> seconds ...</span></div>';
						$('#alertDiv').html(innerHTML);
						countDown();
					}else if(json.code == 1){
						var innerHTML = '<div style="position: absolute; width: 400px;" class="alert alert-error">'
								+ '<button type="button" class="close" data-dismiss="alert">×</button>'
								+ '<span style="text-align: center;">' + json.msg + '</span></div>';
						$('#alertDiv').html(innerHTML);
					}
				});
			}
		});
		
		$('#btnGroupStrategyModel').click(function(e){
			$('#alertErrorDiv').empty();
			$('#groupStrategyFrom')[0].reset();
		});

		$(function() {
			$('#datetimepicker1').datetimepicker();
			$('#datetimepicker2').datetimepicker();
			//domain selector
			$("#domains").select2({
				placeholder : "select which domains to run this ab test",
				allowClear : true
			});
			
			$("#domains").val(initDomains).trigger("change");
			
			<c:forEach var="item" items="${model.groupStrategyList}">
				$('#strategyId option[value=${item.id}]').data(JSON.parse('${item.descriptor}'));
			</c:forEach>
			
			//tips
			$('i[tips]').popover();
			//validate
			$('#form').validation();
			$('#groupStrategyFrom').validation();
		});
	</script>
</a:body>