<%@ page contentType="text/html; charset=utf-8" %>
<!-- GroupStrategy的输入弹出框 -->
<form id="groupStrategyFrom" class="form-horizontal" method="post">
	<div id="groupStrategyModal" class="modal hide fade" tabindex="-1"
		role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal"
				aria-hidden="true">×</button>
			<h3>添加分组策略</h3>
		</div>
		<div class="modal-body" id="groupStrategyDiv1" style="height:400px;" >
			<div class="alert alert-success">
				<strong>Step 1></strong>请粘贴分组策略代码
			</div>
			<div class="control-group">
				<label class="control-label" style="width: 50px;"> </label>
				<div class="controls" style="margin-left: 5px;">
					<textarea id="srcCode" name="srcCode" placeholder="please paste source code here..."
						class="span7" rows="16" cols="80"></textarea>
				</div>
			</div>
		</div>
		<div class="modal-body" style="display:none;height:400px;" id="groupStrategyDiv2">
			<div class="alert alert-success">
				<strong>Step 2></strong>设置类型
			</div>
			<div class="control-group">
				<label class="control-label">Name <i tips=""
					data-trigger="hover" class="icon-question-sign"
					data-toggle="popover" data-placement="top"
					data-original-title="tips"
					data-content="Only charactor, number and underline are allowed. e.g. CatWeb_1"></i>
				</label>
				<div class="controls">
					<input type="text" name="name" placeholder="give it a name ..." id="groupStrategyName"
						check-type="required" required-message="Name is required!">
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Class </label>
				<span class="label label-info" name="name" >Name</span>
			</div>
			<div class="control-group">
				<label class="control-label">FullName </label>
				<span class="label label-info" name="fullyQualifiedName" >Info</span>
			</div>
			<div id="groupStrategyDiv2sub">
			
			</div>
		</div>
		<div class="modal-footer">
			<button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
			<button class="btn btn-primary" id="submitGroupStrategy">Next</button>
			<button class="btn btn-primary" id="submitGroupStrategyOK" data-dismiss="modal" aria-hidden="true" disabled="true">OK</button>
		</div>
	</div>
</form>

<script type="text/javascript">
$(document).ready(function() {
	$("#submitGroupStrategy").click(function(e) {
		e.preventDefault();
		
		if($('#submitGroupStrategy').text() == 'Next'){
			var params = $("#groupStrategyFrom").serialize();
			$.ajax({
				type: "POST",
				url : "abtest?op=parseGs",
				data: params
			}).done(function(json) {
				//alert(json);
				var json = JSON.parse(json);
				$('#submitGroupStrategyOK').data(json);
				
				$("#groupStrategyDiv2 span").each(function(){
					var key = $(this).attr("name");
					$(this).text(json[key]);
				});
				
				var innerHTML = '';
				for(var key in json.fields){
					innerHTML += '<div class="control-group"><label class="control-label">' + json.fields[key]['field-name'] + '</label>'
						+ '<div class="controls"><select name="" check-type="required" required-message="type is required!">'
						+ '<option value="input">input</option><option value="textarea">textarea</option></select>'
						+ '</div></div>';
				}
				
				$('#groupStrategyDiv2sub').empty();
				$('#groupStrategyDiv2sub').html(innerHTML);
				
				$('#submitGroupStrategy').text("Prev");
				$('#submitGroupStrategyOK').removeAttr("disabled");
				
				$('#groupStrategyDiv2').show();
				$('#groupStrategyDiv1').hide();
			}).fail(function(){
				alert("fail");
			});
		}else{
			$('#groupStrategyDiv1').show();
			$('#submitGroupStrategy').text("Next");
			$('#groupStrategyDiv2').hide();
		}

	});
	
	$('#submitGroupStrategyOK').click(function(e){
		e.preventDefault();
		var json = $(this).data();
		
		for(var i in json.fields){
			json.fields[i]['field-type'] = $("#groupStrategyDiv2 select:eq(" + i + ")").val();
		}
		console.log(json);

		var params = {
			'groupStrategyName' : $('#groupStrategyName').val(),
			'groupStrategyClassName' : json.name,
			'groupStrategyFullName' : json.fullyQualifiedName,
			'groupStrategyDescriptor' : JSON.stringify(json),
			'groupStrategyDescription' : ''
		};
		
		$.ajax({
			type: "POST",
			url : "abtest?op=addGs",
			data: params
		}).done(function(json) {
			//$('#groupStrategyModal').modal('hide')
		});
	});
	
	$('#strategyId').change(function(){
		if($(this).val() != 0){
			//alert($(this).val());
			var jsonObject = $('#strategyId option[value=' + $(this).val() + ']').data();
			//var jsonObject = $(this).data();
			var innerHTML = "";
			
			for(var i in jsonObject['fields']){
				var field = jsonObject['fields'][i];
				
				if(field['field-type'] == "textarea"){
					innerHTML += '<div class="control-group"><label class="control-label">' + field['field-name'] + '</label>'
	                         + '<div class="controls"><textarea class="span6" rows="3" cols="60" name="' + field['field-name'] + '"></textarea></div>'
							 + '</div>';
				}else if(field['field-type'] == "input"){
					innerHTML += '<div class="control-group"><label class="control-label">' + field['field-name'] + '</label>'
					         + '<div class="controls"><input type="text" name="' + field['field-name'] + '"></div>'
				             + '</div>';
				}
			}
			
			//alert(innerHTML);
			$('#groupStrategyDivsub').empty();
			$('#groupStrategyDivsub').html(innerHTML);
		}
	});
	
	$('#groupStrategyFrom').validation();
});
</script>