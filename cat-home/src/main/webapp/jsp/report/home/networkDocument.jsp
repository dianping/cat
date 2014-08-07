<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-error">网络监控文档</h3>
</br>
<h4 class="text-info">分为三大监控内容</h4>
<h5>a).核心拓扑，针对公司当前核心网络的实时监控，包括南汇、呼玛机房的路由器及交换机等设备之间的进出流量。</h5>
<h5>b).网络监控，关注于最重要的业务指标，网络监控目的是快速发现网络是否存在问题，一旦出现问题，这类问题对于网络的影响有多大。</h5>
<h5>c).网络监控汇总，将各个不同的网络指标进行汇总，以方便用户进行网络各项指标的横纵向对比。</h5>

</br>
<h4 class="text-success">1. 核心拓扑界面</h4>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/networkTopo.jpeg"/>
<p/>
<p>CTC:电信 &nbsp; CNC:网通 &nbsp; HM:呼玛 &nbsp; NH1:南汇 &nbsp; NH2:南汇</p>
</br>
<h4 class="text-success">2. 网络监控界面</h4>
</br>
<h5>针对每个端口的进出流量进行汇总分析对比</h5>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/networkMetric.jpeg"/>
<p/>
<h5>(1) 关于网络监控的监控指标的确定以及产品线配置请参考业务监控文档</h5>
<h5>(2) 对于网络监控中的指标选取及Key格式规定</h5>
<h4>&nbsp;&nbsp;&nbsp;&nbsp;key = <text class="text-error">{组名}</text>-<text class="text-error">{关键字}</text></h4>
<p>&nbsp;&nbsp;&nbsp;&nbsp;a) 对于需要聚合在一个图表中作对比的所有指标，key中的组名必须完全相同，<text class="text-error">关键字必须不同</text>。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;key=<text class="text-error">GigabitEthernet1/0/0-flow</text>-<text class="text-error">in</text> 与 key=<text class="text-error">GigabitEthernet1/0/0-flow</text>-<text class="text-error">out</text> 将聚合为入一个图表，生成in和out两条曲线，同时组名GigabitEthernet1/0/0-flow作为图表的标题。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;key=<text class="text-error">GigabitEthernet1/0/1-flow</text>-<text class="text-error">in</text> 与 key=<text class="text-error">GigabitEthernet1/0/1-flow</text>-<text class="text-error">out</text> 将聚合为入一个图表，生成in和out两条曲线，同时组名GigabitEthernet1/0/1-flow作为图表的标题。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;b) 关键字与组名之间用“<text class="text-error">-</text>”连接，关键字有字符限制<text class="text-error">（不能包含“-”）</text>，组名没有字符限制。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;c) 不同的组名之间<text class="text-error">不能有被包含关系</text>，如：【<text class="text-error">GigabitEthernet1/0/0</text>-flow】和【<text class="text-error">GigabitEthernet1/0/0</text>】不能作为不同组名同时出现在同一group中。（红色部分为完全相同部分）</p>
<h5>(3) 对于网络的监控代码埋点，仅支持业务监控代码埋点中的HTTP API调用方式。</h5>
<p class="text-error">注意：url中项目组名字(group)必须以"switch-"或"f5-"作为开头</p>
<xmp class="well">
如：http://cat.dp/cat/r/monitor?&timestamp=1404815988&group=f5-2400&domain=2400-com&key=GigabitEthernet1/0/1-flow-in&op=sum&sum=100

</xmp>
<h5>(4) 对于需要在网络监控显示的指标，需对产品线做显示到<a href="/cat/s/config?op=topologyProductLines">网络大盘配置</a>，如下图</h5>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/networkProductLine.jpeg"/>
</br>
