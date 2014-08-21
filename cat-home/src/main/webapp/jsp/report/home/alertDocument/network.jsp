<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4 class="text-success">网络告警</h4>
<h5>网络告警是对网络设备运行状态的监控。通过对流量、接口丢包错包数目、CPU使用率等参数的监控，可以了解网络设备的运行状态。</h5>
<h5>如需了解如何查看核心拓扑界面、查看实时数据等信息，请点击 侧边栏－－网络监控。</h5>
<br/>
<h4 class="text-success">监控规则配置</h4>
<p>网络告警配置在通用规则模型的基础上增加了productText以及metricItemText。网络监控规则模型如下：</p>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/commonRule.png" />
<p>1).点击config－－网络监控配置－－网络告警配置，进入网络规则配置页面。</p>
<p>2).按照overall页面以及上图中的介绍对规则进行配置并提交，如果提示操作成功，则表示规则已经生效。</p>
<br/>
<h4 class="text-success">告警策略配置</h4>
<p>1).点击导航栏Config－－监控告警配置－－告警类型设置</p>
<p>2).编辑id为network的type元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需更改默认策略，请编辑id为default的group元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需增加新的产品线策略，请添加新的group元素，id为产品线名称</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;对group下的level元素进行编辑</p>
<p>3).当出现"操作成功"提示时表明策略已经生效</p>