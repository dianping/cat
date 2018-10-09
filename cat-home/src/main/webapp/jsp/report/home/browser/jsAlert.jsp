<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4 class="text-success">前端JS告警</h4>
<h5>前端JS告警监控JavaScript运行状况。</h5>
<h5>当某一个页面上js运行抛出的Error总数超过设定的阈值时，发出告警信息。</h5>
<br/>
<h4 class="text-success">监控规则配置</h4>
<p>为了满足监控的需求，前端JS监控规则没有采用通用的规则模型。其配置如下：</p>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/alert/frontendException.png" />
<p>各个参数含义如下</p>
<p><span class="text-danger">[模块名称]</span>：JS告警的业务模块</p>
<p><span class="text-danger">[报错等级]</span>：JS错误的等级</p>
<p><span class="text-danger">[告警阈值]</span>：在一分钟内js抛出error个数的上限值。超过这个值就会发出告警</p>
<p><span class="text-danger">[联系人邮件]</span>：告警联系人。多个联系人请用，分开</p>
<br/>
<h4 class="text-success">规则更新</h4>
<p>1).点击config－－JS告警－－进入前端JS监控规则配置页面。</p>
<p>2).点击新增按钮或者选取已有网址的编辑按钮，按照上述介绍对规则进行配置并提交，如果列表页面已更新，则表示规则已经生效。</p>
<br/>
<h4 class="text-success">告警策略配置</h4>
<p>1).点击导航栏Config－－监控告警配置－－告警类型设置</p>
<p>2).编辑id为Js的type元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;只有一个group元素，其id为Js。请不要再添加其它group元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;对group下的level元素进行编辑</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;send属性可为mail,weixin,sms</p>
<p>3).当出现"操作成功"提示时表明策略已经生效</p>