<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav" >
         <ul class="nav nav-list">
           <li class='nav-header'><h4>全局配置</h4></li>
	       <li class="text-right" id="projectList"><a href="?op=projects"><strong>项目信息配置</strong></a></li>
	       <li class="text-right" id="topologyProductLines"><a href="?op=topologyProductLines"><strong>项目分组配置</strong></a></li>
           <li class='nav-header'><h4>应用监控配置</h4></li>
	       <li class="text-right" id="topylogyNodeConfigList"><a href="?op=topologyGraphNodeConfigList"><strong>拓扑节点阀值</strong></a></li>
	       <li class="text-right" id="topylogyEdgeConfigList"><a href="?op=topologyGraphEdgeConfigList"><strong>拓扑依赖阀值</strong></a></li>
	       <li class="text-right" id="exceptionConfigList"><a href="?op=exceptionThresholds"><strong>异常阀值配置</strong></a></li>
           <li class='nav-header'><h4>外部监控配置</h4></li>
	       <li class="text-right" id="urlPatternList"><a href="?op=urlPatterns"><strong>URL合并规则</strong></a></li>
	       <li class="text-right" id="aggregationList"><a href="?op=aggregations"><strong>JS合并规则</strong></a></li>
           <li class='nav-header'><h4>业务监控配置</h4></li>
	       <li class="text-right" id="metricConfigList"><a href="?op=metricConfigList"><strong>业务监控规则</strong></a></li>
	       <li class="text-right" id="metricRuleConfigUpdate"><a href="?op=metricRuleConfigUpdate"><strong>业务告警配置</strong></a></li>
	       <li class="text-right" id="metricGroupConfigUpdate"><a href="?op=metricGroupConfigUpdate"><strong>业务指标分组</strong></a></li>
	       <li class="text-right" id="metricAggregationConfigUpdate"><a href="?op=metricAggregationConfigUpdate"><strong>业务指标汇总</strong></a></li>
           <li class='nav-header'><h4>报表展示配置</h4></li>
	       <li class="text-right" id="bugConfigUpdate"><a href="?op=bugConfigUpdate"><strong>异常规范配置</strong></a></li>
	       <li class="text-right" id="domainGroupConfigUpdate"><a href="?op=domainGroupConfigUpdate"><strong>机器分组配置</strong></a></li>
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

