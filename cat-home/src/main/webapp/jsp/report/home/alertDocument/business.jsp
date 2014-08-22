<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4 class="text-success">业务告警</h4>
<h5>业务告警是对项目业务指标的监控。</h5>
<h5>如需了解如何增加指标、在代码中埋点、配置告警通知人等信息，请点击 侧边栏－－业务监控。</h5>
<br/>
<h4 class="text-success">监控规则配置</h4>
<p>1).业务告警的监控规则是以项目为单位的。</p>
<p>2).点击config－－业务监控配置－－业务监控规则，进入业务配置页面。</p>
<p>3).在业务配置页面中先选择项目所属的产品线，然后在项目列表中找到项目，并点击最右侧的按钮“规则设置”。</p>
<p>4).按照overall页面中的介绍对规则进行配置并提交，如果提示操作成功，则表示规则已经生效。</p>
<br/>
<h4 class="text-success">告警策略配置</h4>
<p>1).点击导航栏Config－－监控告警配置－－告警类型设置</p>
<p>2).编辑id为business的type元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需更改默认策略，请编辑id为default的group元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需增加新的产品线策略，请添加新的group元素，id为产品线名称</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;对group下的level元素进行编辑</p>
<p>3).当出现"操作成功"提示时表明策略已经生效</p>