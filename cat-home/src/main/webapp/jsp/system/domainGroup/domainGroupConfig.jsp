<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<script src="/cat/js/jquery.nestable.min.js"></script>

			<form name="domainGroupConfigUpdate" id="form" method="post"
				action="${model.pageUri}?op=domainGroupConfigUpdate">
				<h4 class="text-center text-danger">【请不要修改其他项目节点，新增自己项目节点即可】</h4>
				<table class="table table-striped table-condensed   table-hover">
					<tr>
						<td><textarea name="content" style="width:100%" rows="20" cols="150">${model.content}</textarea></td>
					</tr>
					<tr>
						<td style="text-align:center"><input class='btn btn-primary' 
							type="submit" name="submit" value="提交" /></td>
					</tr>
				</table>
			</form>
			<h4 class="text-center text-danger" id="state">&nbsp;</h4>
			
			<div class="row">
				<div class="col-xs-12">
					<div class="row">
						<div class="col-sm-6">
							<div class="dd" id="nestable">
								<ol class="dd-list">
									<li class="dd-item dd2-item" data-id="2">
										<div class="dd-handle dd2-handle">
											<i class="normal-icon ace-icon fa fa-check-square-o blue bigger-150"></i>
											<i class="drag-icon ace-icon fa fa-arrows bigger-125"></i>
										</div>
										<div class="dd2-content">
											&nbsp;&nbsp;项目: &nbsp;&nbsp;<input value="default"></input>
											<div class="pull-right action-buttons">
												<a class="red" href="#">
													<i class="ace-icon fa fa-trash-o bigger-130"></i>
												</a>
											</div>
										</div>
										<ol class="dd-list">
											<li class="dd-item dd2-item" data-id="22">
												<div class="dd-handle dd2-handle">
													<i class="normal-icon ace-icon fa fa-users blue bigger-150"></i>
													<i class="drag-icon ace-icon fa fa-arrows bigger-125"></i>
												</div>
												<div class="dd2-content">
													&nbsp;&nbsp;Group: &nbsp;&nbsp;<input value="default"></input>
													<div class="pull-right action-buttons">
														<a class="red" href="#">
															<i class="ace-icon fa fa-trash-o bigger-130"></i>
														</a>
													</div>
												</div>
												<ol class="dd-list">
													<li class="dd-item dd2-item" data-id="22">
														<div class="dd-handle dd2-handle">
															<i class="normal-icon ace-icon fa fa-desktop blue bigger-150"></i>
															<i class="drag-icon ace-icon fa fa-arrows bigger-125"></i>
														</div>
														<div class="dd2-content">
															&nbsp;&nbsp;机器IP: &nbsp;&nbsp;<input value="default"></input>
															<div class="pull-right action-buttons">
																<a class="red" href="#">
																	<i class="ace-icon fa fa-trash-o bigger-130"></i>
																</a>
															</div>
														</div>
													</li>
												</ol>
											</li>
										</ol>
									</li>
								</ol>
							</div>
						</div>
					</div>
				</div>
			</div>
</a:config>
<script type="text/javascript">
		$(document).ready(function() {
			$('#projects_config').addClass('active open');
			$('#domainGroupConfigUpdate').addClass('active');
			var state = '${model.opState}';
			if(state=='Success'){
				$('#state').html('操作成功');
			}else{
				$('#state').html('操作失败');
			}
			setInterval(function(){
				$('#state').html('&nbsp;');
			},3000);
			
			$('.dd').nestable();
			$('.dd-handle a').on('mousedown', function(e){
			   e.stopPropagation();
			});
		});
	</script>