<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">1、确定要监控的对象</h4>
<pre>
<span class="text-danger">命令字：</span> 在CAT的移动监控中，是以命令字作为监控对象 <a href="/cat/s/app?op=appList" target="_blank">查看及配置[可添加修改]</a>

	1) SDK上传具体监控对象，一般是每条url请求的path最后部分，h5加载监控的命令字是完整的url字符串。
	   如shop.bin, http://e.meishi.st.sankuai.com/api/homepage/menumoduleconfig
	   
	2) 命令字可以配置别名，别名在页面左侧的Config/App监控/API命令字中进行配置。
	3) 为了便于查找，命令字输入框中加入了模糊匹配辅助功能，输入命令字的一部分可以快速提示匹配的命令字搜索列表。
	4) 每个命令字的数据可以合并到某个命令字里，即分组的概念。意义在于，方便统计一个命令字集合内的所有数据的汇总统计。<a href="/cat/s/app?op=appCommandGroup" target="_blank">点此配置</a>
	
</pre>
	<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor05.png"/>
<br/><br/>
<pre>
用户可以在上图所示界面对Command命令字进行修改操作

1) 单个添加
   <span class="text-danger">名称：</span>命令字（如：shop.bin，api.baymax.adp.meituan.com, http://bddeal.meishi.sankuai.com/m/list等）
   <span class="text-danger">App: </span>命令字属于哪个App
   <span class="text-danger">项目名：</span>对应的服务项目名，会根据此项目名查找需要发送告警的联系人信息(告警人信息来源CMDB)
   <span class="text-danger">标题：</span>方便记住的标题名字
   <span class="text-danger">默认过滤时间：</span>响应时间超过该阈值的访问数据不予统计
2) 批量
   <span class="text-danger">名称：</span>输入格式（命令字名称1|命令字标题1;命令字名称2|命令字标题2;...）
   	不设置标题默认展示命令字名称，所以格式也可为（命令字名称1;命令字名称2;...）
3) 上图最上面的分类为App分类，下面的左侧分类为命令字所归属的项目名，方便用户查找命令字
</pre>
<br/>
<h4 class="text-danger">2、了解监控的维度</h4>
<p>监控的分析的维度有来源、返回码、网络类型、APP版本、连接类型、平台、地区、运营商等。</p>
<pre>

<span class="text-danger">来源:</span>    该命令字的请求发起来自哪个App。<span class="text-danger">当你的命令字不属于现有的App时，可添加新的App</span> <a href="/cat/s/app?op=appSources" target="_blank">查看及配置[可添加]</a>
	
<span class="text-danger">返回码:</span>  1) API请求的返回码，数据来自SDK中的上报。<a href="/cat/s/app?op=appCodes" target="_blank">查看及配置[可添加修改]</a>
	2) 返回码可以配置别名，别名在页面左侧的Config/App监控/返回码中进行配置。
	3) 返回码配置中可以配置返回码代表的是成功还是失败，目前除了200表示成功以外，还有450 451等返回码被配置为表示成功。
	4) 返回码在每个APP中有全局设置，即对于此App中所有命令字的默认的返回码设置。
	5) 每个命令字可以根据自己需求进行更多返回码设置，甚至覆盖全局设置的返回码，这部分返回成为局部返回码，状态表示代表成功或失败
	6) <span class="text-danger">当命令字所属APP的全局返回码中没有你想要的返回码，可以在该命令字的局部返回码中添加，前提是先添加命令字，即完成第一个步骤</span>
	
<span class="text-danger">网络类型:</span> API请求时的网络状况，数据来自SDK中的上报。目前网络类型包括：ALL/WIFI/2G/3G/4G/UNKNOWN <a href="/cat/s/app?op=appConstants" target="_blank">查看[固定]</a>

<span class="text-danger">版本:</span>    APP的版本号，SDK中自动为每条记录增加APP的版本号。<a href="/cat/s/app?op=appConstants" target="_blank">查看及配置[可添加]</a>

<span class="text-danger">连接类型:</span> 网络请求走的连接通道，数据来自SDK中的上报。目前连接类型包括：ALL/短连接/长连接/UDP连接/WNS连接/HTTPS连接 <a href="/cat/s/app?op=appConstants" target="_blank">查看[固定]</a>

<span class="text-danger">平台:</span>    客户端类型。SDK中自动为每条记录增加平台代码。目前平台包括：ALL/android/ios/Unknown <a href="/cat/s/app?op=appConstants" target="_blank">查看[固定]</a>

<span class="text-danger">地区:</span>    API请求发生时，客户端所在的城市或地区。数据是大众点评内部的服务根据IP反查出来的，反查IP的数据库来自腾讯，准确率99% <a href="/cat/s/app?op=appConstants" target="_blank">查看[固定]</a>

<span class="text-danger">运营商:</span>  1) API请求发生时，客户端网络所使用的运营商。与地区类似，运营商数据也是根据IP反查出来的。 <a href="/cat/s/app?op=appConstants" target="_blank">查看[固定]</a>
	2) 目前运营商包括：ALL/中国移动/中国联通/中国电信/中国铁通/其他/国外其他/教育网

</pre><br/><br/>
