<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4 class="text-success">系统告警</h4>
<h5>系统告警是对服务器运行状态的监控。通过对CPU、内存、硬盘等参数的监控，可以了解服务器的运行状态。</h5>
<h5>目前paas平台上的服务器已采用Cat进行系统监控。其它服务器暂时继续使用zabbix进行系统监控</h5>
<br/>
<h4 class="text-success">监控规则配置</h4>
<p>系统告警配置在通用规则模型的基础上增加了productText以及metricItemText。系统监控规则模型如下：</p>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/alert/commonRule.png" />
<p>1).点击 配置－－监控告警配置－－系统告警配置，进入系统规则配置页面。</p>
<p>2).按照overall页面以及上图中的介绍对规则进行配置并提交，如果提示操作成功，则表示规则已经生效。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;由于系统监控规则一般即为所有服务器的通用监控规则，一般不设置productText属性。</p>
<br/>
<h4 class="text-success">告警策略配置</h4>
<p>1).点击导航栏 配置－－监控告警配置－－告警类型设置</p>
<p>2).编辑id为system的type元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需更改默认策略，请编辑id为default的group元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需增加新的产品线策略，请添加新的group元素，id为产品线名称</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;对group下的level元素进行编辑</p>
<p>3).当出现"操作成功"提示时表明策略已经生效</p>