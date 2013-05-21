<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav">
         <ul class="nav nav-list">
           <li class='nav-header'><h4>全局配置信息</h4></li>
	       <li id="projectList"><a href="?op=projects"><strong>项目基本信息配置</strong></a></li>
	       <li id="aggregationList"><a href="?op=aggregations"><strong>前端监控规则配置</strong></a></li>
	       <li id="bussinessConfigList"><a href="?"><strong>业务监控规则配置</strong></a></li>
         </ul>
</div>
<style>
	.nav-list  li  a{
		padding:2px 15px;
	}
	.nav li  +.nav-header{
		margin-top:2px;
	}
	.nav-header{
		padding:5px 3px;
	}
</style>

