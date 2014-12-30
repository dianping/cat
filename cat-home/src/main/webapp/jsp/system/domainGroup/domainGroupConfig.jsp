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
	<h4 class="text-center text-danger" id="state">&nbsp;</h4>
	<style>
		.dd-handle, .dd2-content{
			margin:0px 0px 0px 0px;
		}
	</style>
	<div class="page-content-area">
		&nbsp;&nbsp;&nbsp;&nbsp;<button id="add-domain" type="button" class="btn btn-success btn-sm">添加新项目</button>
		<button id="submit" type="button" class="btn btn-primary btn-sm">提交</button>
		<div class="row">
			<div class="col-xs-12 col-md-8">
			<div class="dd" id="nestable">
				<ol class="dd-list" id="domain-list">
					<li class="dd-item dd2-item delete-item domain-item">
						<div class="dd2-content">
							&nbsp;&nbsp;项目: &nbsp;&nbsp;<input class="domain-id" value="default"></input>
							<div class="pull-right action-buttons">
								<a class="green add-group" href="javascript:void(0)">
									<i class="ace-icon glyphicon glyphicon-plus"></i>
									添加Group
								</a>
								<a class="red delete-button" href="javascript:void(0)">
									<i class="ace-icon fa fa-trash-o bigger-130"></i>
									删除
								</a>
							</div>
						</div>
						<ol class="dd-list group-list">
							<li class="dd-item dd2-item delete-item group-item" data-id="22">
								<div class="dd2-content">
									&nbsp;&nbsp;Group: &nbsp;&nbsp;<input value="default" class="group-id"></input>
									<div class="pull-right action-buttons">
										<a class="green add-ip" href="javascript:void(0)">
											<i class="ace-icon glyphicon glyphicon-plus"></i>
											添加ip
										</a>
										<a class="red delete-button" href="javascript:void(0)">
											<i class="ace-icon fa fa-trash-o bigger-130"></i>
											删除
										</a>
									</div>
								</div>
								<ol class="dd-list ip-list">
									<li class="dd-item dd2-item delete-item ip-item" data-id="22">
										<div class="dd2-content">
											&nbsp;&nbsp;机器IP: &nbsp;&nbsp;<input value="default" class="ip-id"></input>
											<div class="pull-right action-buttons">
												<a class="red delete-button" href="javascript:void(0)">
													<i class="ace-icon fa fa-trash-o bigger-130"></i>
													删除
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
		
		$('[data-action="collapse"]').click();
		$('.dd').nestable();
		$('.dd-handle a').on('mousedown', function(e){
		   e.stopPropagation();
		});
		$('[data-action="collapse"]').trigger('click');
		
		window.domainSample = $('.domain-item').eq(0).clone();
		window.groupSample = $('.group-item').eq(0).clone();
		window.ipSample = $('.ip-item').eq(0).clone();
		
		init();
	});
	function init(){
		initDomains();
		addListener();
	}
	function initDomains(){
		var content = '${model.content}';
		var config;
		try {
			config = JSON.parse(content);
        } catch (e) {
            alert("读取机器信息错误！请刷新重试或联系leon.li@dianping.com");
            return;
        }
        
        var domains = config['domains'];
        window.isFirstDomain = true;
        
        for(var key in domains){
        	var currentDomain = domains[key];
        	initDomain(currentDomain);
        }
	}
	function initDomain(domain){
		var title = domain['id'];
		var groups = domain['groups'];
		
		if(isFirstDomain){
			isFirstDomain = false;
		}else{
			$('#domain-list').append(domainSample.clone());
		}
		
		var domainView = $('.domain-item').last();
		
		domainView.find('.domain-id').val(title);
		
		var firstGroup = true;
		
		for(var key in groups){
			var currentGroup = groups[key];
			
			if(firstGroup){
				initGroup(currentGroup, domainView, true);
				firstGroup = false;
			}else{
				initGroup(currentGroup, domainView, false);
			}
		}		
	}
	function initGroup(currentGroup, domainView, firstGroup){
		var title = currentGroup['id'];
		var ips = currentGroup['ips'];
		
		if(!firstGroup){
			domainView.find('.group-list').append(groupSample.clone());
		}
		var groupView = domainView.find('.group-item').last();
		
		groupView.find('.group-id').val(title);
		
		var firstIp = true;
		
		for(var count in ips){
			var currentIp = ips[count];
			if(firstIp){
				firstIp = false;
			}else{
				groupView.find('.ip-list').append(ipSample.clone());
			}
			
			groupView.find('.ip-item').last().find('.ip-id').val(currentIp);
		}		
	}
	function addListener(){
		$('#add-domain').click(function(e){
			$('#domain-list').append(domainSample.clone());
		});
		$('.page-content-area').delegate('.add-group', 'click', function(e){
			addNewItem(this, '.domain-item', '.group-list', groupSample);
		});
		$('.page-content-area').delegate('.add-ip', 'click', function(e){
			addNewItem(this, '.group-item', '.ip-list', ipSample);
		});
		$('.page-content-area').delegate('.delete-button', 'click', function(e){
			$(this).parents(".delete-item").eq(0).remove();
		});
		$('#submit').click(function(){
			var content = generateContent();
			
			if(content!=""){
				$('#content').val(content);
				$('#form').submit();  
			}
		});
	}
	function addNewItem(currentElement, parentElement, targetElement, sample){
		$(currentElement).parents(parentElement).eq(0).children(targetElement).append(sample.clone());
	}
	function generateContent(){
		var jsonObj = {};
		var domains = {};
		
		jsonObj['domains'] = domains;
		$('.domain-item').each(function(){
			var domain = {};
			var groups = {};
			var title = $(this).find('.domain-id').val();
			
			domain['id'] = title;
			domain['groups'] = groups;
			generateGroups(this, groups);
			
			domains[title] = domain;
		});
		try{
			return JSON.stringify(jsonObj);
		} catch (e) {
            alert("生成机器信息错误！请检查配置是否有误或联系leon.li@dianping.com");
            return "";
        }
	}
	function generateGroups(currentElement, groups){
		$(currentElement).find('.group-item').each(function(){
			var group = {};
			var title = $(this).find('.group-id').val();
			var ips = [];
			
			group['id'] = title;
			group['ips'] = ips;
			
			$(this).find('.ip-item').each(function(){
				var ip = $(this).find('.ip-id').val();
				
				ips.push(ip);
			});
			
			groups[title] = group;
		})
	}
</script>