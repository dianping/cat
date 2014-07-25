<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav" >
         <ul class="nav nav-list">
           <li class='nav-header'><h4>全局配置</h4></li>
	       <li class="text-right" id="projectList"><a href="?op=projects"><strong>项目信息配置</strong></a></li>
	       <li class="text-right" id="topologyProductLines"><a href="?op=topologyProductLines"><strong>项目分组配置</strong></a></li>
           <li class='nav-header'><h4>应用监控配置</h4></li>
	       <li class="text-right" id="topylogyNodeConfigList"><a href="?op=topologyGraphNodeConfigList"><strong>拓扑节点阀值</strong></a></li>
	       <li class="text-right" id="topylogyEdgeConfigList"><a href="?op=topologyGraphEdgeConfigList"><strong>拓扑依赖阀值</strong></a></li>
	       <li class="text-right" id="exceptionConfigList"><a href="?op=exception"><strong>异常告警配置</strong></a></li>
           <li class='nav-header'><h4>外部监控配置</h4></li>
	       <li class="text-right" id="urlPatternList"><a href="?op=urlPatterns"><strong>URL合并规则</strong></a></li>
	       <li class="text-right" id="aggregationList"><a href="?op=aggregations"><strong>JS合并规则</strong></a></li>
	       <li class="text-right" id="appConfigUpdate"><a href="?op=appConfigUpdate"><strong>APP监控配置</strong></a></li>
	       <li class="text-right" id="thirdPartyConfigUpdate"><a href="?op=thirdPartyConfigUpdate"><strong>第三方监控配置</strong></a></li>
           <li class='nav-header'><h4>业务监控配置</h4></li>
	       <li class="text-right" id="metricConfigList"><a href="?op=metricConfigList"><strong>业务监控规则</strong></a></li>
	       <li class="text-right" id="metricGroupConfigUpdate"><a href="?op=metricGroupConfigUpdate"><strong>业务指标分组</strong></a></li>
           <li class='nav-header'><h4>网络监控配置</h4></li>
	       <li class="text-right" id="networkRuleConfigUpdate"><a href="?op=networkRuleConfigUpdate"><strong>网络告警配置</strong></a></li>
	       <li class="text-right" id="netGraphConfigUpdate"><a href="?op=netGraphConfigUpdate"><strong>网络拓扑配置</strong></a></li>
	       <li class='nav-header'><h4>系统监控配置</h4></li>
	       <li class="text-right" id="systemRuleConfigUpdate"><a href="?op=systemRuleConfigUpdate"><strong>系统告警配置</strong></a></li>
	       <li class='nav-header'><h4>系统默认配置</h4></li>
	       <li class="text-right" id="alertDefaultReceivers"><a href="?op=alertDefaultReceivers"><strong>默认告警配置</strong></a></li>
	       <li class="text-right" id="policy"><a href="?op=alertPolicy"><strong>告警类型设置</strong></a></li>
	       <li class="text-right" id="bugConfigUpdate"><a href="?op=bugConfigUpdate"><strong>异常类型配置</strong></a></li>
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

