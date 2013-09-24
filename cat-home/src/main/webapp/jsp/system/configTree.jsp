<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav" >
         <ul class="nav nav-list">
           <li class='nav-header'><h4>全局配置</h4></li>
	       <li class="text-right" id="projectList"><a href="?op=projects"><strong>项目信息配置</strong></a></li>
	       <li class="text-right" id="topologyProductLines"><a href="?op=topologyProductLines"><strong>产品线配置</strong></a></li>
           <li class='nav-header'><h4>拓扑图配置</h4></li>
	       <li class="text-right" id="topylogyNodeConfigList"><a href="?op=topologyGraphNodeConfigList"><strong>拓扑节点阀值</strong></a></li>
	       <li class="text-right" id="topylogyEdgeConfigList"><a href="?op=topologyGraphEdgeConfigList"><strong>拓扑依赖阀值</strong></a></li>
	       <li class="text-right" id="exceptionConfigList"><a href="?op=exceptionThresholds"><strong>异常阀值配置</strong></a></li>
           <li class='nav-header'><h4>其他配置</h4></li>
	       <li class="text-right" id="metricConfigList"><a href="?op=metricConfigList"><strong>业务监控规则</strong></a></li>
	       <li class="text-right" id="bugConfigUpdate"><a href="?op=bugConfigUpdate"><strong>异常规范配置</strong></a></li>
	       <li class="text-right" id="aggregationList"><a href="?op=aggregations"><strong>前端合并规则</strong></a></li>
	       <li class="text-right" id="utilizationConfigUpdate"><a href="?op=utilizationConfigUpdate"><strong>资源权重配置</strong></a></li>
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
	.row-fluid .span2{
		width:12%;
	}
</style>

