<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav" >
         <ul class="nav nav-list">
           <li class='nav-header'>全局配置</li>
	       <li class="text-right" id="projectList"><a href="?op=projects">项目信息配置</a></li>
	       <li class="text-right" id="topologyProductLines"><a href="?op=topologyProductLines">项目分组配置</a></li>
           <li class='nav-header'>应用监控配置</li>
	       <li class="text-right" id="topylogyNodeConfigList"><a href="?op=topologyGraphNodeConfigList">拓扑节点阀值</a></li>
	       <li class="text-right" id="topylogyEdgeConfigList"><a href="?op=topologyGraphEdgeConfigList">拓扑依赖阀值</a></li>
	       <li class="text-right" id="exceptionConfigList"><a href="?op=exception">异常告警配置</a></li>
           <li class='nav-header'>外部监控配置</li>
	       <li class="text-right" id="urlPatternList"><a href="?op=urlPatterns">URL合并规则</a></li>
	       <li class="text-right" id="aggregationList"><a href="?op=aggregations">JS合并规则</a></li>
	       <li class="text-right" id="appConfigUpdate"><a href="?op=appConfigUpdate">APP监控配置</a></li>
	       <li class="text-right" id="thirdPartyConfigUpdate"><a href="?op=thirdPartyConfigUpdate">第三方监控配置</a></li>
           <li class='nav-header'>业务监控配置</li>
	       <li class="text-right" id="metricConfigList"><a href="?op=metricConfigList">业务监控规则</a></li>
	       <li class="text-right" id="metricGroupConfigUpdate"><a href="?op=metricGroupConfigUpdate">业务指标分组</a></li>
           <li class='nav-header'>网络监控配置</li>
	       <li class="text-right" id="networkRuleConfigUpdate"><a href="?op=networkRuleConfigUpdate">网络告警配置</a></li>
	       <li class="text-right" id="netGraphConfigUpdate"><a href="?op=netGraphConfigUpdate">网络拓扑配置</a></li>
	       <li class='nav-header'>监控告警配置</li>
	       <li class="text-right" id="systemRuleConfigUpdate"><a href="?op=systemRuleConfigUpdate">系统告警配置</a></li>
	       <li class="text-right" id="alertDefaultReceivers"><a href="?op=alertDefaultReceivers">默认告警配置</a></li>
	       <li class="text-right" id="policy"><a href="?op=alertPolicy">告警类型设置</a></li>
	       <li class="text-right" id="bugConfigUpdate"><a href="?op=bugConfigUpdate">异常类型配置</a></li>
	       <li class='nav-header'>其他监控配置</li>
	       <li class="text-right" id="domainGroupConfigUpdate"><a href="?op=domainGroupConfigUpdate">机器分组配置</a></li>
	       <li class="text-right" id="routerConfigUpdate"><a href="?op=routerConfigUpdate">客户端路由配置</a></li>
         </ul>
</div>
<style>
	.nav-list  li  a{
		padding:0px 15px;
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

