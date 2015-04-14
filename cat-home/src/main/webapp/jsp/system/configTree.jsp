<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav" >
         <ul class="nav nav-list">
           <li class='nav-header'>项目配置</li>
	       <li class="text-left" id="projectList"><a href="?op=projects">项目信息配置</a></li>
	       <li class="text-left" id="topologyProductLines"><a href="?op=topologyProductLines">监控分组配置</a></li>
	       <li class="text-left" id="domainGroupConfigUpdate"><a href="?op=domainGroupConfigUpdate">机器分组配置</a></li>
           <li class='nav-header'>端到端监控配置</li>
	       <li class="text-left" id="aggregationList"><a href="?op=aggregations">JS报错配置</a></li>
	       <li class="text-left" id="urlPatternList"><a href="?op=urlPatterns">WEB监控配置</a></li>
	       <li class="text-left" id="webRule"><a href="?op=webRule">WEB告警配置</a></li>
	       <li class="text-left" id="appList"><a href="?op=appList">APP监控配置</a></li>
	       <li class="text-left" id="appConfigUpdate"><a href="?op=appConfigUpdate" style="display:none">APP全局配置</a></li>
	       <li class="text-left" id="appRule"><a href="?op=appRule">APP告警配置</a></li>
	       <li class="text-left" id="appComparisonConfigUpdate"><a href="?op=appComparisonConfigUpdate">美团对比报表</a></li>
           <li class='nav-header'>应用监控配置</li>
	       <li class="text-left" id="metricConfigList"><a href="?op=metricConfigList">业务监控规则</a></li>
	       <li class="text-left" id="transactionRule"><a href="?op=transactionRule">Transaction告警</a></li>
	       <li class="text-left" id="metricRuleConfigUpdate"><a href="?op=metricRuleConfigUpdate" style="display:none">业务全局规则</a></li>
	       <li class="text-left" style="display:none" id="domainMetricRuleConfigUpdate"><a href="?op=domainMetricRuleConfigUpdate">业务XML规则</a></li>
	       <li class="text-left" id="exceptionConfigList"><a href="?op=exception">异常告警配置</a></li>
	       <!-- <li class="text-left" id="bugConfigUpdate"><a href="?op=bugConfigUpdate">框架异常配置</a></li> -->
	       <li class="text-left" id="displayPolicy"><a href="?op=displayPolicy">心跳报表配置</a></li>
	       <li class="text-left" id="heartbeatRuleConfigList"><a href="?op=heartbeatRuleConfigList">心跳告警配置</a></li>
	       <li class="text-left" id="thirdPartyConfigUpdate"><a href="?op=thirdPartyConfigUpdate">第三方告警配置</a></li>
	       <li class="text-left" id="topylogyNodeConfigList"><a href="?op=topologyGraphNodeConfigList">应用阀值配置</a></li>
	       <li class="text-left" id="topylogyEdgeConfigList"><a href="?op=topologyGraphEdgeConfigList">应用依赖配置</a></li>
	       <li class='nav-header'>监控告警配置</li>
	       <li class="text-left" id="networkRuleConfigList"><a href="?op=networkRuleConfigList">网络告警配置</a></li>
	       <li class="text-left" id="netGraphConfigUpdate"><a href="?op=netGraphConfigUpdate">网络拓扑配置</a></li>
	       <li class="text-left" id="databaseRuleConfigList"><a href="?op=databaseRuleConfigList">数据库告警配置</a></li>
	       <li class="text-left" id="storageGroupConfigUpdate"><a href="storageGroupConfigUpdate">数据库分组配置</a></li>
	       <li class="text-left" id="systemRuleConfigList"><a href="?op=systemRuleConfigList">系统告警配置</a></li>
	       <li class="text-left" id="policy"><a href="?op=alertPolicy">告警策略配置</a></li>
	       <li class="text-left" id="alertDefaultReceivers"><a href="?op=alertDefaultReceivers">默认告警人配置</a></li>
	       <li class="text-left" id="routerConfigUpdate"><a href="?op=routerConfigUpdate">客户端路由配置</a></li>
         </ul>
</div>

