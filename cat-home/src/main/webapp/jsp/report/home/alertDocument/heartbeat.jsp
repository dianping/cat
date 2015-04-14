<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4 class="text-success">心跳告警</h4>
<h5>心跳告警是对服务器当前状态的监控，如监控系统负载、GC数量等信息。</h5>
<br/>
<h4 class="text-success">监控规则配置</h4>
<p>心跳告警是由两级匹配的。首先匹配项目，然后按照指标匹配。指标由下拉框选择。</p>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/alert/heartbeatRule.png" />
<p>1).点击config－－应用监控配置－－心跳告警配置，进入心跳规则配置页面。</p>
<p>2).按照overall页面以及上图中的介绍对规则进行配置并提交，如果提示操作成功，则表示规则已经生效。</p>
<br/>
<h4 class="text-success">告警策略配置</h4>
<p>1).点击导航栏 配置－－监控告警配置－－告警类型设置</p>
<p>2).编辑id为heartbeat的type元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需更改默认策略，请编辑id为default的group元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需增加新的项目策略，请添加新的group元素，id为项目名称</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;对group下的level元素进行编辑</p>
<p>3).当出现"操作成功"提示时表明策略已经生效</p>