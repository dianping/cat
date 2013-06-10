<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav">
         <ul class="nav nav-list">
           <li class='nav-header'><h4>全局配置</h4></li>
	       <li id="projectList"><a href="?op=projects"><strong>项目基本信息</strong></a></li>
           <li class='nav-header'><h4>前端合并配置</h4></li>
	       <li id="aggregationList"><a href="?op=aggregations"><strong>前端合并规则</strong></a></li>
           <li class='nav-header'><h4>拓扑图配置</h4></li>
	       <li id="topylogyNodeConfigList"><a href="?op=topologyGraphNodeConfigList"><strong>拓扑节点阀值</strong></a></li>
	       <li id="topylogyEdgeConfigList"><a href="?op=topologyGraphEdgeConfigList"><strong>拓扑依赖阀值</strong></a></li>
	       <li id="topologyProductLines"><a href="?op=topologyProductLines"><strong>产品线配置</strong></a></li>
           <li class='nav-header'><h4>业务监控配置</h4></li>
	       <li id="bussinessConfigList"><a href="?"><strong>业务监控规则</strong></a></li>
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

