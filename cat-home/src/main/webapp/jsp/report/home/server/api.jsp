<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h5 class="text-success">Metric-broker-service接口调用请求说明</h5>
<pre>
	http请求方式: POST（请使用http协议）
	http://metric-broker.dp/metric-broker-service/api/metric
</pre>
<p>参数说明</p>
<table style="width:90%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">参数</th><th>说明</th></tr>
	<tr><td>data</td><td>要发送的Json数据，<span class="text-danger">必需，需要Url Encode</span></td></tr>
</table>
<p>Json数据</p>
<pre><code>{
    "category":"network",
    "entities":[
        {
            "measure":"network.flow-in",
            "timestamp":1451268386119,
            "tags":{
                "port":"1",
                "endPoint":"switch-01"
            },
            "fields":{
                "value":0.7220096548596434
            }
        },
        {
            "measure":"network.flow-in",
            "timestamp":1451268386119,
            "tags":{
                "port":"2",
                "endPoint":"switch-01"
            },
                "fields":{
            	"value":0.19497605734770518
            }
        }
    ]
}
</code></pre>
<table style="width:90%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">属性</th><th>说明</th></tr>
	<tr><td>category</td><td>监控分类，包括system, network, database；<span class="text-danger">必需，不能包含特殊字符</span></td></tr>
	<tr><td>measure</td><td>监控指标名称，<span class="text-danger">必需，以“category.”开头。例如，网络设备进口流量：“network.flow-in”</span></td></tr>
	<tr><td>timestamp</td><td>监控数据产生的时间戳，<span class="text-danger">必需，毫秒为单位的时间，注意值不要加引号</span></td></tr>
	<tr><td>tags</td><td>标签,<span class="text-danger">必需，且必须有endPoint这个key，endPoint值为当前监控对象的唯一ID；</span>如有其它标签，可以添加，一般适用于网络设备端口等。</td></tr>
	<tr><td>fields</td><td>采集的监控数据<span class="text-danger">必须，一般只需一个key：{"value":data}，注意value值不要加引号</td></tr>
</table>

<p> 示例
<pre>
	http://metric-broker.dp/metric-broker-service/api/metric?data={encodedData}
</pre>
<p>返回说明</p>
<pre>
	<span class="text-danger">{"status":500, "info":"failed"}  ——> 失败</span>
	<span class="text-success">{"status":200, "info":"success"} ——> 成功</span>
</pre>