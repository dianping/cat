<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4 class="text-success">数据库告警</h4>
<h5>数据库告警是对数据库运行状态的监控。</h5>
<br/>
<h4 class="text-success">监控规则配置</h4>
<p>数据库告警配置在通用规则模型的基础上增加了productText以及metricItemText。数据库监控规则模型如下：</p>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/alert/commonRule.png" />
<p>1).点击 配置－－监控告警配置－－数据库告警配置，进入数据库规则配置页面。</p>
<p>2).按照overall页面以及上图中的介绍对规则进行配置并提交，如果提示操作成功，则表示规则已经生效。</p>
<br/>
<h4 class="text-success">告警策略配置</h4>
<p>1).点击导航栏 配置－－监控告警配置－－告警类型设置</p>
<p>2).编辑id为database的type元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需更改默认策略，请编辑id为default的group元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需增加新的产品线策略，请添加新的group元素，id为产品线名称</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;对group下的level元素进行编辑</p>
<p>3).当出现"操作成功"提示时表明策略已经生效</p>