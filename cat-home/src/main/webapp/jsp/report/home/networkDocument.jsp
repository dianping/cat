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
<h5>针对每个端口的进出流量进行汇总分析对比，同时对每个端口的进出的错包、丢包情况进行汇总对比。</h5>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/networkMetric.jpeg"/>
<p/>
<p>(1) 关于网络监控的监控指标的确定以及产品线配置请参考业务监控文档</p>
<p>(2) 对于网络监控中的指标选取及Key格式规定</p>
<xmp class="well">
a) 每个端口的进出流量汇总
   [端口名字] + [-in]  --> 进端口流量KEY
   [端口名字] + [-out] --> 出端口流量KEY
	 	
b) 每个端口的错包丢包情况汇总 
   [端口名字] + [-inerrors]  --> 进端口错包数KEY
   [端口名字] + [-outerrors] --> 出端口错包数KEY
   [端口名字] + [-indiscards]  --> 进端口丢包数KEY
   [端口名字] + [-outdiscards] --> 出端口丢包数KEY
</xmp>

<p>(2) 对于网络的监控代码埋点，仅支持业务监控代码埋点中的HTTP API调用方式。</p>
<p class="text-error">注意：url中项目组名字(group)必须以"switch-"或"f5-"作为开头</p>
<xmp class="well">
如：http://cat.dianpingoa.com/cat/r/monitor?group=f5-2400&domain=2400-1-dianping-com&key=Ethernet1/1-1-in&op=sum&sum=100
</xmp>
<p>(3) 对于需要在网络监控显示的指标，需对产品线做显示到<a href="/cat/s/config?op=topologyProductLines">网络大盘配置</a>，如下图</p>
<img  class="img-polaroid"  width='40%' src="${model.webapp}/images/networkProductLine.jpeg"/>
</br>
</br> 
<h4 class="text-success">3. 监控汇总定制</h4>
<p>除了默认汇总展示之外，用户可以根据不同需求对网络指标进行汇总定制，在<a href="/cat/s/config?op=metricAggregationConfigUpdate">业务指标汇总</a>中填写配置信息。</p>
<h5>(1) 配置说明</h5>
<table style="width:90%" class="table table-striped table-bordered table-condensed">
	<tr><th width="20%">节点</th><th width="20%">属性</th><th>说明</th></tr>
	<tr>
		<td rowspan="4" style="vertical-align:middle">metric-aggregation-group</td>
		<td>id</td>
		<td>汇总组唯一ID名称，<span class="text-error">必需，</span>仅仅包括字母、数字，不能包含特殊字符，比如引号，冒号等。建议TuanGou这类命名方式</td>
	<tr>
		<td>type</td>
		<td>指标类型，<span class="text-error">必需，</span>对于网络监控固定设置为<span class="text-error">“Metric”</span></td>
	</tr>
	<tr>
		<td>display</td>
		<td>显示类型，<span class="text-error">必需，</span>对于网络监控固定设置为<span class="text-error">“network”</span></td>
	</tr>
	<tr>
		<td>metric-aggregation</td>
		<td>汇总图形集合，<span class="text-error">必需</td>
	</tr>
	
	<tr>
		<td rowspan="8" style="vertical-align:middle">metric-aggregation</td>
		<td>id</td>
		<td>汇总图形唯一ID名称，<span class="text-error">必需，</span>仅仅包括字母、数字，不能包含特殊字符，比如引号，冒号等。建议TuanGou这类命名方式</td>
	<tr>
	<tr>
		<td>domain</td>
		<td>网络指标来源的项目组，<span class="text-error">必需，</span>项目组名</td>
	</tr>
	<tr>
		<td>title</td>
		<td>作为图形标题的显示<span class="text-error">非必需，</span>默认为id</td>
	</tr>
		<td>display-type</td>
		<td>显示指标分类，<span class="text-error">必需，</span>代表所要显示的值是平均值、总和还是次数，可选项为：<span class="text-error">count、sum、avg</span></td>
	</tr>
	<tr>
		<td>base-line</td>
		<td>是否显示基线，<span class="text-error">非必需，</span>可选项为：true、false，默认为<span class="text-error">false</span></td>
	</tr>
	<tr>
		<td>operation</td>
		<td>数学运算操作，<span class="text-error">非必需，</span>将数据进行数学四则运算后再展示，默认不做处理</td>
	</tr>
	<tr>
		<td>metric-aggregation</td>
		<td>汇总曲线集合，<span class="text-error">必需</td>
	</tr>
	<tr>
		<td rowspan="6" style="vertical-align:middle">metric-aggregation</td>
		<td>key</td>
		<td>汇总曲线唯一ID名称，<span class="text-error">必需，</span>对应每个指标的key字段，必须与网络参数指标发送中的key保持一致。</td>
	<tr>
	<tr>
		<td>domain</td>
		<td>网络指标来源的项目组，<span class="text-error">非必需，</span>默认继承自父节点的domain值</td>
	</tr>
	<tr>
		<td>operation</td>
		<td>数学四则运算操作，<span class="text-error">非必需，</span>默认继承自父节点的operation值</td>
	</tr>
		<td>display-type</td>
		<td>显示指标分类，<span class="text-error">非必需，</span>代表所要显示的值是平均值、总和还是次数，可选项为：<span class="text-error">count、sum、avg</span>，默认继承自父节点的display-type值</td>
	</tr>
	<tr>
		<td>base-line</td>
		<td>是否显示基线，<span class="text-error">非必需，</span>，可选项为true、false，默认继承自父节点的base-line值</td>
	</tr>
</table>
<h5>(2) <a href="/cat/s/config?op=metricAggregationConfigUpdate">指标汇总配置</a>实例如下：</h5>
<xmp class="well">
<metric-aggregation-config>
	<metric-aggregation-group id="f5-2400-1-dianping-com-flow" type="Metric" display="network">
		<metric-aggregation id="f5-2400-1-in-flow" display-type="count" base-line="false" operation="{data}*60+100">
			<metric-aggregation-item domain="Domain1" key="1/1-1-in" operation="{data}*60/100+100" />
			<metric-aggregation-item domain="Domain2" key="1/1-2-in" />
			<metric-aggregation-item domain="Domain3" key="1/1-3-in" />
			<metric-aggregation-item domain="Domain4" key="1/1-4-in" />
		</metric-aggregation>
		<metric-aggregation id="f5-2400-1-out-flow" domain="Domain" display="sum" base-line="true">
			<metric-aggregation-item domain="Domain2" key="1/1-1-out" display-type="count" base-line="false"/>
			<metric-aggregation-item key="1/1-2-out"/>
			<metric-aggregation-item key="1/1-3-out"/>
			<metric-aggregation-item key="1/1-4-out"/>
		</metric-aggregation>
	</metric-aggregation-group>
	<metric-aggregation-group id="f5-2400-1-dianping-com-packages" domain="DmainAll" type="Metric" display="network">
		<metric-aggregation id="f5-2400-1-in-packages" display-type="sum" title="switch">
			<metric-aggregation-item key="1/1-1-inerrors" />
			<metric-aggregation-item key="1/1-2-inerrors" />
			<metric-aggregation-item key="1/1-3-inerrors" />
			<metric-aggregation-item key="1/1-4-inerrors" />
			<metric-aggregation-item key="1/1-1-indiscards" />
			<metric-aggregation-item key="1/1-2-indiscards" />
			<metric-aggregation-item key="1/1-3-indiscards" />
			<metric-aggregation-item key="1/1-4-indiscards" />
		</metric-aggregation>
		<metric-aggregation id="f5-2400-1-out-packages" display-type="sum">
			<metric-aggregation-item key="1/1-1-outerrors" />
			<metric-aggregation-item key="1/1-2-outerrors" />
			<metric-aggregation-item key="1/1-3-outerrors" />
			<metric-aggregation-item key="1/1-4-outerrors" />
			<metric-aggregation-item key="1/1-1-outdiscards" />
			<metric-aggregation-item key="1/1-2-outdiscards" />
			<metric-aggregation-item key="1/1-3-outdiscards" />
			<metric-aggregation-item key="1/1-4-outdiscards" />
		</metric-aggregation>
	</metric-aggregation-group>
</metric-aggregation-config>
</xmp>
