<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">Business实时报表 <a href="/cat/r/business?domain=cat">访问链接</a></h4> 
<h5>公司核心业务指标监控</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/metric02.png" width="100%"/>
<h5>输入时间，查询条件，即可查看业务指标报表。查询条件可以是domain，也可以是标签名称，其中标签均以TAG_开头。</h5>
<h5 class='text-danger'>“当前值”表示当前实际值，“基线值”表示根据历史趋势算出来当天的基准线</h5>
<br/><br/>
<h4 class="text-success">自定义metric配置 <a href="/cat/s/business?op=list">配置链接</a></h4>
<h5>支持自定义metric，自定义metric指对多个打点得到的指标进行四则运算，得到一个新的metric。</h5>

<h5 class="text-success">配置方法：</h5>
<h5>输入domain，查询 -> 点击新增，进入配置页面 -> 填写自定义metric的信息，在规则配置栏中填写四则运算规则 -> 配置完成后，即可在对应domain的报表中看到该metric的报表。</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/metric03.png" width="100%"/>
<h4 class="text-success">一个例子：<br/></h4>
<h5 class="text-success">应用打点：</h5>
<h5>
	在应用shopweb的某段代码中，加入如下打点：<br/><br/>
	Cat.logMetricForCount("success");<br/>
	Cat.logMetricForCount("total", 3);<br/>
</h5>
<h5 class="text-success">自定义配置：</h5>
<h5>\${shopweb,success,COUNT} / \${shopweb,total,COUNT} </h5>
<h5>表示shopweb下名为success,类型为COUNT的值除以shopweb下名为total，类型为COUNT的值，用于表示成功率。</h5>
<h5 class='text-danger'>最终shopweb这个domain下，business报表会有三张图，表示的指标分别为success，total，以及计算出的成功率。</h5>
<br/><br/>
<h4 class="text-success">标签配置：<a href="/cat/s/business?op=tagConfig">配置链接</a><br/></h4>
<h5>每个tag节点表示一个标签，tag下可以有多个business-item,每个business-item表示一个metric项，其中domain为应用名，item-id为指标名称。</h5><br/>
<img  class="img-polaroid"  src="${model.webapp}/images/metric04.png" width="100%"/>
	
