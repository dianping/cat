<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-danger">1. 网络监控总述</h3>
<br/>
<h4 class="text-info">分为三大监控内容</h4>
<h5>a).核心拓扑，针对公司当前核心网络的实时监控，包括南汇、呼玛机房的路由器及交换机等设备之间的进出流量。</h5>
<h5>b).网络监控，关注于最重要的业务指标，网络监控目的是快速发现网络是否存在问题，一旦出现问题，这类问题对于网络的影响有多大。</h5>
<h5>c).网络监控汇总，将各个不同的网络指标进行汇总，以方便用户进行网络各项指标的横纵向对比。</h5>

<br/>
<h4 class="text-success">1. 核心拓扑界面</h4>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/alert/network01.jpeg"/>
<p/>
<p>CTC:电信 &nbsp; CNC:网通 &nbsp; HM:呼玛 &nbsp; NH1:南汇 &nbsp; NH2:南汇</p>
<br/>
<h4 class="text-success">2. 网络监控界面</h4>
<br/>
<h5>针对每个端口的进出流量进行汇总分析对比</h5>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/alert/network02.jpeg"/>
<p/>
<h5>(1) 关于网络监控的监控指标的确定以及产品线配置请参考业务监控文档</h5>
<h5>(2) 对于网络监控中的指标选取及Key格式规定</h5>
<h4>&nbsp;&nbsp;&nbsp;&nbsp;key = <text class="text-danger">{组名}</text>-<text class="text-danger">{关键字}</text></h4>
<p>&nbsp;&nbsp;&nbsp;&nbsp;a) 对于需要聚合在一个图表中作对比的所有指标，key中的组名必须完全相同，<text class="text-danger">关键字必须不同</text>。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;key=<text class="text-danger">GigabitEthernet1/0/0-flow</text>-<text class="text-danger">in</text> 与 key=<text class="text-danger">GigabitEthernet1/0/0-flow</text>-<text class="text-danger">out</text> 将聚合为入一个图表，生成in和out两条曲线，同时组名GigabitEthernet1/0/0-flow作为图表的标题。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;key=<text class="text-danger">GigabitEthernet1/0/1-flow</text>-<text class="text-danger">in</text> 与 key=<text class="text-danger">GigabitEthernet1/0/1-flow</text>-<text class="text-danger">out</text> 将聚合为入一个图表，生成in和out两条曲线，同时组名GigabitEthernet1/0/1-flow作为图表的标题。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;b) 关键字与组名之间用“<text class="text-danger">-</text>”连接，关键字有字符限制<text class="text-danger">（不能包含“-”）</text>，组名没有字符限制。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;c) 不同的组名之间<text class="text-danger">不能有被包含关系</text>，如：【<text class="text-danger">GigabitEthernet1/0/0</text>-flow】和【<text class="text-danger">GigabitEthernet1/0/0</text>】不能作为不同组名同时出现在同一group中。（红色部分为完全相同部分）</p>
<h5>(3) 对于网络的监控代码埋点，仅支持业务监控代码埋点中的HTTP API调用方式。</h5>
<p class="text-danger">注意：url中项目组名字(group)必须以"switch-"或"f5-"作为开头</p>
<xmp class="well">
如：http://cat.dp/cat/r/monitor?&timestamp=1404815988&group=f5-2400&domain=2400-com&key=GigabitEthernet1/0/1-flow-in&op=sum&sum=100

</xmp>
<h5>(4) 对于需要在网络监控显示的指标，需对产品线做显示到<a href="/cat/s/config?op=topologyProductLines">网络大盘配置</a>，如下图</h5>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/alert/network03.png"/>
<br/><br/>

<h3 class="text-danger">2. 网络告警</h3>
<h5>网络告警是对网络设备运行状态的监控。通过对流量、接口丢包错包数目、CPU使用率等参数的监控，可以了解网络设备的运行状态。</h5>
<h5>如需了解如何查看核心拓扑界面、查看实时数据等信息，请点击 侧边栏－－网络监控。</h5>
<br/>
<h4 class="text-success">监控规则配置</h4>
<p>网络告警配置在通用规则模型的基础上增加了productText以及metricItemText。网络监控规则模型如下：</p>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/alert/commonRule.png" />
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