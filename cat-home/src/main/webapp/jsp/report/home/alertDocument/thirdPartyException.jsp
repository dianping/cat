<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4 class="text-success">第三方告警</h4>
<h5>第三方告警是根据指定的网址发送HTTP请求，当返回码不为200时发送警告。</h5>
<h5>第三方告警是一种HeartBeat检测，可以监控一个web app的可用性和网络状况</h5>
<br/>
<h4 class="text-success">监控规则配置</h4>
<p>为了满足第三方监控的需求，第三方监控规则没有采用通用的规则模型。其规则模型如下：</p>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/thirdPartyAlert.png" />
<p>具体配置如下</p>
<p><span class="text-error">[url]</span>：监控的网址</p>
<p><span class="text-error">[type]</span>：<span class="text-error">get</span> 或 <span class="text-error">post</span></p>
<p><span class="text-error">[domain]</span>：依赖于该第三方的项目名，会向该项目组联系人发第三方告警</p>
<p><span class="text-error">[par]</span>：请求中包含的参数，<span class="text-error">id</span>为参数名称，<span class="text-error">value</span>为参数值</p>
<p>以上参数名均为小写。监控周期为一分钟。</p>
<br/>
<h4 class="text-success">规则更新</h4>
<p>1).点击config－－外部监控配置－－第三方监控配置，进入第三方监控规则配置页面。</p>
<p>2).按照上述介绍对规则进行配置并提交，如果提示操作成功，则表示规则已经生效。</p>
<br/>
<h4 class="text-success">告警策略配置</h4>
<p>1).点击导航栏Config－－监控告警配置－－告警类型设置</p>
<p>2).编辑id为thirdParty的type元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需更改默认策略，请编辑id为default的group元素</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;如需增加新的产品线策略，请添加新的group元素，id为项目名称，与规则配置中的domain属性相对应</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;对group下的level元素进行编辑</p>
<p>3).当出现"操作成功"提示时表明策略已经生效</p>